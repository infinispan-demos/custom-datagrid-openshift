package app;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.vertx.reactivex.core.Context;
import io.vertx.reactivex.core.Vertx;
import org.infinispan.client.hotrod.RemoteCache;

final class RxCacheImpl<K, V> implements RxCache<K, V> {

   final RemoteCache<K, V> cache;
   final Vertx vertx;

   RxCacheImpl(RemoteCache<K, V> cache, Vertx vertx) {
      this.cache = cache;
      this.vertx = vertx;
   }

   @Override
   public Completable put(K key, V value) {
      return getContext()
         .rxExecuteBlocking(
            f -> {
               cache.put(key, value);
               f.complete();
            }
         )
         .ignoreElement();
   }

   @Override
   public Maybe<V> get(K key) {
      return getContext()
         .<V>rxExecuteBlocking(
            f -> {
               V v = cache.get(key);
               f.complete(v);
            }
         )
         .flatMapMaybe(value ->
            value != null
               ? Maybe.just(value)
               : Maybe.empty()
         );
   }

   private Context getContext() {
      return vertx.getOrCreateContext();
   }

}
