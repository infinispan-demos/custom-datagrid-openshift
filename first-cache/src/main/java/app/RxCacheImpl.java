package app;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import org.infinispan.client.hotrod.RemoteCache;

final class RxCacheImpl<K, V> implements RxCache<K, V> {

   final RemoteCache<K, V> remote;

   RxCacheImpl(RemoteCache<K, V> remote) {
      this.remote = remote;
   }

   @Override
   public Completable put(K key, V value) {
      return null;  // TODO: Customise this generated block
   }

   @Override
   public Maybe<V> get(K key) {
      return null;  // TODO: Customise this generated block
   }

}
