package app;

import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import java.util.logging.Logger;

public class Main extends AbstractVerticle {

   static final Logger log = Logger.getLogger(Main.class.getName());

   private InfinispanRx infinispan;

   @Override
   public void start(io.vertx.core.Future<Void> future) {
      Router router = Router.router(vertx);
//      router.get("/create-cache").handler(this::test);
//      router.get("/get-cache").handler(this::test);

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
      
   }

   private void getCache(RoutingContext rc) {
      ConfigurationBuilder testCfg = new ConfigurationBuilder();

      testCfg
         .addServer()
         .host("datagrid-app-hotrod")
         .port(11333); // TODO why 11333?

      final RemoteCacheManager remote = new RemoteCacheManager(testCfg.build());

      final RemoteCache<String, String> cache = remote.getCache("custom-cache");

      cache.put("hello", "world");
      String value = cache.get("hello");

      rc.response().end("Value is: " + value);
   }

}
