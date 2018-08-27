package app;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.Vertx;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

final class InfinispanRx {

   private final RemoteCacheManager remote;

   private InfinispanRx(RemoteCacheManager remote) {
      this.remote = remote;
   }

   static Single<InfinispanRx> connect(
      ConfigurationBuilder cfg
      , Vertx vertx
   ) {
      return vertx
         .rxExecuteBlocking(remoteCacheManager(cfg))
         .map(InfinispanRx::new);
   }

   private static Handler<Future<RemoteCacheManager>> remoteCacheManager(
      ConfigurationBuilder cfg
   ) {
      return f -> f.complete(new RemoteCacheManager(cfg.build()));
   }

}
