package com.atypon.nosqldbserver.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DBSchema {

    private final String name;
    private final List<DBCollection> collections;

    public DBSchema(@JsonProperty("name") String name) {
        this.name = name;
        this.collections = new ArrayList<>();
    }

    public void addCollection(DBCollection collection) {
        this.collections.add(collection);
    }

    public boolean dropCollection(String collectionName) {
        return this.collections.removeIf(col -> col.getName().equals(collectionName));
    }

}
