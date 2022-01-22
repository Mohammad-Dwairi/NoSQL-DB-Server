package com.atypon.nosqldbserver.request;

import lombok.Getter;

import java.beans.ConstructorProperties;

@Getter
public class Pair<K, V> {

    private final K key;
    private final V value;

    @ConstructorProperties({"key", "value"})
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
