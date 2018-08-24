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

   @Override
   public void start(io.vertx.core.Future<Void> future) {
      Router router = Router.router(vertx);
      router.get("/test").handler(this::test);

      vertx
         .createHttpServer()
         .requestHandler(router::accept)
         .rxListen(8080)
         .subscribe(
            server -> {
               log.info("Http server started");
               future.complete();
            }
            , future::fail
         );
   }

   private void test(RoutingContext rc) {
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
