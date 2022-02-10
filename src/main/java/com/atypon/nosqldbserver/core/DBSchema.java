package com.atypon.nosqldbserver.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
public class DBSchema {

    private final String name;
    private final List<DBCollection> collections;

    private DBSchema(@JsonProperty("name") String name) {
        this.name = name;
        this.collections = new ArrayList<>();
    }

    public static DBSchema create(@NonNull String name) {
        return new DBSchema(name);
    }

    public void addCollection(DBCollection collection) {
        this.collections.add(collection);
    }

    public boolean dropCollection(String collectionName) {
        return this.collections.removeIf(col -> col.getName().equals(collectionName));
    }

}
