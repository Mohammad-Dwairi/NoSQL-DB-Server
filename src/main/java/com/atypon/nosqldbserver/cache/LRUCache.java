package com.atypon.nosqldbserver.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class LRUCache<K, V> {

    private final int capacity;
    private final LinkedHashMap<K, V> cache;

    public LRUCache(@Value("${atypon.db.cache.capacity}") int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity);
    }

    public Optional<V> get(K key) {
        if (cache.containsKey(key)) {
            V value = cache.remove(key);
            cache.put(key, value);
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public void put(K key, V value) {
        if (cache.size() >= capacity) {
            Map.Entry<K, V> eldestEntry = cache.entrySet().iterator().next();
            cache.remove(eldestEntry.getKey());
        }
        cache.put(key, value);
    }

    public void drop(K key) {
        cache.remove(key);
    }
}
