package mykidong.raft.db;

import java.util.Optional;

public interface KVStore<K, V> {
    boolean save(K key, V value);
    Optional<V> find(K key, Class<V> valueClass);
    boolean delete(K key);
}
