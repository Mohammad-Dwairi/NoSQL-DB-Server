package com.atypon.nosqldbserver.index;

import com.atypon.nosqldbserver.utils.JSONUtils;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class DBIndex<K, V> {

    protected final String path;
    protected final Map<K, V> indexMap;

    public String getPath() {
        return path;
    }

    public Optional<V> get(K key) {
        V v = indexMap.get(key);
        if (v == null) {
            return Optional.empty();
        }
        return Optional.of(v);
    }

    public List<V> get(List<K> keys) {
        return indexMap.keySet().stream().filter(keys::contains).map(indexMap::get).collect(Collectors.toList());
    }

    public void put(K key, V value) {
        this.indexMap.put(key, value);
    }

    public List<V> values() {
        return new ArrayList<>(this.indexMap.values());
    }

    public void drop(K key) {
        indexMap.remove(key);
    }

    public int size() {
        return indexMap.size();
    }

    public String toJSON() {
        return JSONUtils.convertToJSON(this.indexMap);
    }


}
