package com.atypon.nosqldbserver.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public class DBCollection {


    private final String name;
    private final Map<String, String> schema;

    public DBCollection(@JsonProperty("collectionName") String name, @JsonProperty("collectionSchema") Map<String, String> schema) {
        this.name = name;
        this.schema = schema;
    }
    
}
