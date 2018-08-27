package app;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

public interface RxMap<K, V> {

   Completable put(K key, V value);

   Maybe<V> get(K key);

}
