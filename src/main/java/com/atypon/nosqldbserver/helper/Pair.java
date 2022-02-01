package com.atypon.nosqldbserver.helper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.beans.ConstructorProperties;

@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Pair<K, V> {

    private K key;
    private V value;

    @ConstructorProperties({"key", "value"})
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
