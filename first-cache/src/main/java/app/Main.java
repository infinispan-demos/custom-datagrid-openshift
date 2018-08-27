package app;

import io.reactivex.Single;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends AbstractVerticle {

   static final Logger log = Logger.getLogger(Main.class.getName());

   private InfinispanRx infinispan;

   private String prefix = UUID.randomUUID().toString();
   private int counter;
   private String cacheName;

   @Override
   public void start(io.vertx.core.Future<Void> future) {
      Router router = Router.router(vertx);
      router.get("/create-cache").handler(this::createCache);
      router.get("/get-cache").handler(this::getCache);

      ConfigurationBuilder cfg = new ConfigurationBuilder();

      cfg
         .addServer()
         .host("caching-service-app-hotrod")
         .port(11222);

      vertx
         .createHttpServer()
         .requestHandler(router::accept)
         .rxListen(8080)
         .flatMap(s -> InfinispanRx.connect(cfg, vertx))
         .subscribe(
            infinispan -> {
               log.info("Http server started and connected to Infinispan");
               this.infinispan = infinispan;
               future.complete();
            }
            , future::fail
         );
   }

   private void createCache(RoutingContext rc) {
      cacheName = String.format("%s-%s", prefix, counter++);

      infinispan
         .createCache(cacheName, vertx)
         .subscribe(
            x ->
               rc.response().end(String.format("Cache %s created", cacheName))
            ,
            failure -> {
               log.log(Level.SEVERE, "Failure starting injectors", failure);
               rc.response().end("Failure starting injectors: " + failure);
            }
         );
   }

   private void getCache(RoutingContext rc) {
      infinispan
         .getCache(cacheName, vertx)
         .flatMap(rxCache ->
            rxCache
               .put("sample-key", "sample-value")
               .andThen(Single.just(rxCache))
         )
         .flatMapMaybe(rxCache ->
            rxCache.get("sample-key")
         )
         .subscribe(
            x ->
               rc.response().end(String.format(
                  "Got cache, put/get returned: %s", x
               ))
            ,
            failure -> {
               log.log(Level.SEVERE, "Failure starting injectors", failure);
               rc.response().end("Failure starting injectors: " + failure);
            }
         );
   }

}
