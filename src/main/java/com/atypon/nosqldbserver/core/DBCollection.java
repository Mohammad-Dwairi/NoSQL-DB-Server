package com.atypon.nosqldbserver.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;

@Getter
@ToString
@EqualsAndHashCode
public class DBCollection {

    private final String name;
    private final Object schema;

    public DBCollection(@JsonProperty("collectionName") String name, @JsonProperty("collectionSchema") Object schema) {
        this.name = name;
        this.schema = schema;
    }
    
}
