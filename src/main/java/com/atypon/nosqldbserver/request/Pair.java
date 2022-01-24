package com.atypon.nosqldbserver.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.ConstructorProperties;

@Data
@NoArgsConstructor
public class Pair<K, V> {

    private K key;
    private V value;

    @ConstructorProperties({"key", "value"})
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.key == null && this.value == null;
    }
}
