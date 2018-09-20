package app;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.commons.api.CacheContainerAdmin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends AbstractVerticle {

   private static final Logger log = Logger.getLogger(Main.class.getName());

   private RemoteCacheManager cacheManager;

   private String prefix = UUID.randomUUID().toString();
   private int counter;
   private String cacheName;

   @Override
   public void start(Future<Void> startFuture) {
      Router router = Router.router(vertx);
      router.get("/connect/:svcName").handler(this::connect);
      router.get("/create-cache").handler(this::createCache);
      router.get("/destroy-cache").handler(this::destroyCache);
      router.get("/get-cache").handler(this::getCache);

      vertx
         .createHttpServer()
         .requestHandler(router::accept)
         .listen(8080, res -> {
            if (res.succeeded()) {
               log.info("Http server started");
               startFuture.complete();
            } else {
               startFuture.fail(res.cause());
            }
         });
   }

   private void connect(RoutingContext rc) {
      String svcName = rc.request().getParam("svcName");

      ConfigurationBuilder cfg =
         DatagridCfg.create(svcName + "-hotrod", svcName);

      vertx.executeBlocking(
         cacheManager(cfg)
         , res -> {
            if (res.succeeded()) {
               this.cacheManager = res.result();
               rc.response().end("Infinispan connection successful");
            } else {
               final Throwable failure = res.cause();
               log.log(Level.SEVERE, "Unable to connect to Infinispan", failure);
               rc.response().end("Unable to connect to Infinispan: " + failure);
            }
         }
      );
   }

   private void createCache(RoutingContext rc) {
      cacheName = String.format("%s-%s", prefix, counter++);

      vertx.executeBlocking(
         createCache(cacheName, cacheManager)
         , res -> {
            if (res.succeeded()) {
               rc.response().end(String.format("Cache %s created", cacheName));
            } else {
               final Throwable failure = res.cause();
               log.log(Level.SEVERE, "Failure creating cache", failure);
               rc.response().end("Failure creating cache: " + failure);
            }
         }
      );
   }

   private void destroyCache(RoutingContext rc) {
      vertx.executeBlocking(
         destroyCache(cacheName, cacheManager)
         , res -> {
            if (res.succeeded()) {
               rc.response().end(String.format("Cache %s destroyed", cacheName));
            } else {
               final Throwable failure = res.cause();
               log.log(Level.SEVERE, "Failure destroying cache", failure);
               rc.response().end("Failure destroying cache: " + failure);
            }
         }
      );
   }

   private void getCache(RoutingContext rc) {
      vertx.executeBlocking(
         Main.<String, String>getCache(cacheName, cacheManager)
         , res -> {
            if (res.succeeded()) {
               final RemoteCache<String, String> cache = res.result();

               // Do a put on the cache asynchronously
               final CompletableFuture<String> cachePut =
                  cache.putAsync("sample-key", "sample-value");

               // Then do a get on the cache asynchronously
               final CompletableFuture<String> cacheGet = cachePut
                  .thenCompose(prev -> cache.getAsync("sample-key"));

               cacheGet
                  .whenComplete((value, failure) -> {
                     if (failure == null) {
                        rc.response().end(String.format(
                           "Got cache, put/get returned: %s", value
                        ));
                     } else {
                        final Throwable putGetFailure = res.cause();
                        log.log(Level.SEVERE, "Failure doing put/get on cache", putGetFailure);
                        rc.response().end("Failure doing put/get on cache: " + putGetFailure);
                     }
                  });
            } else {
               final Throwable getCacheFailure = res.cause();
               log.log(Level.SEVERE, "Failure getting cache", getCacheFailure);
               rc.response().end("Failure getting cache: " + getCacheFailure);
            }
         }
      );
   }

   private static Handler<Future<RemoteCacheManager>> cacheManager(
      ConfigurationBuilder cfg
   ) {
      return f -> f.complete(new RemoteCacheManager(cfg.build()));
   }

   private static <K, V> Handler<Future<RemoteCache<K, V>>> createCache(
      String cacheName
      , RemoteCacheManager cacheManager
   ) {
      return f -> {
         try {
            final RemoteCache<K, V> cache = cacheManager
               .administration()
               .withFlags(CacheContainerAdmin.AdminFlag.PERMANENT)
               .createCache(cacheName, "replicated");

            f.complete(cache);
         } catch (HotRodClientException e) {
            f.fail(e);
         }
      };
   }

   private static Handler<Future<Void>> destroyCache(
      String cacheName
      , RemoteCacheManager remote
   ) {
      return f -> {
         try {
            remote.administration()
               .withFlags(CacheContainerAdmin.AdminFlag.PERMANENT)
               .removeCache(cacheName);

            f.complete(null);
         } catch (HotRodClientException e) {
            f.fail(e);
         }
      };
   }

   private static <K, V> Handler<Future<RemoteCache<K, V>>> getCache(
      String cacheName
      , RemoteCacheManager remote
   ) {
      return f -> f.complete(remote.getCache(cacheName));
   }

}
