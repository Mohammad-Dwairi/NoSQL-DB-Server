package com.atypon.nosqldbserver.service.collection;

import com.atypon.nosqldbserver.core.DBCollection;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.Pair;

import java.util.List;
import java.util.Optional;

public interface CollectionService {

    List<DBCollection> findAll(String schemaName);

    Optional<DBCollection> find(CollectionId collectionId);

    void create(String schemaName, DBCollection collection);

    void drop(CollectionId collectionId);

    void createRequestedIndex(CollectionId colReq, String key);

    List<Pair<String, String>> getRegisteredIndexes(CollectionId collectionId);

    void recoverExistingDocuments(CollectionId collectionId, String indexedPropertyName);
}
