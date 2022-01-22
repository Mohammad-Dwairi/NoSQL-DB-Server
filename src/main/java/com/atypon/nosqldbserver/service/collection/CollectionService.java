package com.atypon.nosqldbserver.service.collection;

import com.atypon.nosqldbserver.core.DBCollection;
import com.atypon.nosqldbserver.request.CollectionRequest;

import java.util.List;
import java.util.Optional;

public interface CollectionService {

    List<DBCollection> findAll(String schemaName);
    Optional<DBCollection> find(CollectionRequest collectionRequest);
    void create(String schemaName, DBCollection collection);
    void drop(CollectionRequest collectionRequest);
    void createIndex(CollectionRequest colReq, String key);
}
