package app;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.Vertx;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.CacheContainerAdmin;

final class InfinispanRx {

   private final RemoteCacheManager remote;

   private InfinispanRx(RemoteCacheManager remote) {
      this.remote = remote;
   }

   <K, V> Single<RxCache<K, V>> createCache(
      String cacheName
      , Vertx vertx
   ) {
      return vertx
         .rxExecuteBlocking(InfinispanRx.<K, V>createCache(cacheName, remote))
         .map(cache -> new RxCacheImpl<>(cache, vertx));
   }

   <K, V> Single<RxCache<K, V>> getCache(
      String cacheName
      , Vertx vertx
   ) {
      return vertx
         .rxExecuteBlocking(InfinispanRx.<K, V>getCache(cacheName, remote))
         .map(cache -> new RxCacheImpl<>(cache, vertx));
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

   private static <K, V> Handler<Future<RemoteCache<K, V>>> createCache(
      String cacheName
      , RemoteCacheManager remote
   ) {
      return f -> f.complete(
         remote.administration()
            .withFlags(CacheContainerAdmin.AdminFlag.PERMANENT)
            .createCache(cacheName, "replicated")
      );
   }

   private static <K, V> Handler<Future<RemoteCache<K, V>>> getCache(
      String cacheName
      , RemoteCacheManager remote
   ) {
      return f -> f.complete(remote.getCache(cacheName));
   }

}
