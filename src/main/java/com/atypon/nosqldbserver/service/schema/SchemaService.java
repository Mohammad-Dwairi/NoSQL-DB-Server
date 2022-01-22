package com.atypon.nosqldbserver.service.schema;

import com.atypon.nosqldbserver.core.DBSchema;

import java.util.List;
import java.util.Optional;

public interface SchemaService {

    List<DBSchema> findAll();

    Optional<DBSchema> find(String schema);

    void create(String name);

    void drop(String name);

    void writeToSchemaFile(List<DBSchema> schemas);

}
