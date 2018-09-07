package app;

import io.reactivex.Single;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import java.util.ArrayList;
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
      router.get("/connect/:svcName").handler(this::connect);
      router.get("/create-cache").handler(this::createCache);
      router.get("/destroy-cache").handler(this::destroyCache);
      router.get("/get-cache/:numCalls").handler(this::getCache);

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

   private void connect(RoutingContext rc) {
      String svcName = rc.request().getParam("svcName");

      ConfigurationBuilder cfg =
         InfinispanCfg.create(svcName + "-hotrod", svcName);

      InfinispanRx.connect(cfg, vertx)
         .subscribe(
            infinispan -> {
               this.infinispan = infinispan;
               rc.response().end("Infinispan connection successful");
            }
            , t -> {
               log.log(Level.SEVERE, "Unable to connect to Infinispan", t);
               rc.response().end("Unable to connect to Infinispan: " + t);
            }
         );
   }

   private void createCache(RoutingContext rc) {
      cacheName = String.format("%s-%s", prefix, counter++);

      infinispan
         .createCache(cacheName, vertx)
         .subscribe(
            x ->
               rc.response().end(String.format("Cache %s created", cacheName))
            , failure -> {
               log.log(Level.SEVERE, "Failure creating cache", failure);
               rc.response().end("Failure creating cache: " + failure);
            }
         );
   }

   private void destroyCache(RoutingContext rc) {
      infinispan
         .destroyCache(cacheName, vertx)
         .subscribe(
            () ->
               rc.response().end(String.format("Cache %s destroyed", cacheName))
            , failure -> {
               log.log(Level.SEVERE, "Failure destroying cache", failure);
               rc.response().end("Failure destroying cache: " + failure);
            }
         );
   }

   private void getCache(RoutingContext rc) {
      String numCallsParam = rc.request().getParam("numCalls");
      int numCalls = Integer.parseInt(numCallsParam);

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
         .repeat(numCalls)
         .reduce(
            new ArrayList<>()
            , (l, result) -> {
               l.add(result);
               return l;
            })
         .subscribe(
            x ->
               rc.response().end(String.format(
                  "Got cache, %d calls to put/get returned: %s", numCalls, x
               ))
            , failure -> {
               log.log(Level.SEVERE, "Failure starting injectors", failure);
               rc.response().end("Failure starting injectors: " + failure);
            }
         );
   }

}
