package app;

import io.reactivex.Completable;
import io.reactivex.Maybe;

interface RxCache<K, V> {

   Completable put(K key, V value);

   Maybe<V> get(K key);

}
