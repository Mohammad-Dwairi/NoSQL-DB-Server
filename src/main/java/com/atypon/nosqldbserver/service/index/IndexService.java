package com.atypon.nosqldbserver.service.index;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.request.CollectionRequest;
import com.atypon.nosqldbserver.request.DocumentRequest;
import com.atypon.nosqldbserver.request.Pair;

import java.util.LinkedHashMap;
import java.util.List;

public interface IndexService {

    LinkedHashMap<String, List<DBDocumentLocation>> load(String path);

    void createIndex(String indexedOn, CollectionRequest colReq);

    void reIndex(CollectionRequest request, String indexedOn);

    void addToIndex(String indexPath, String value, DBDocumentLocation location);

    void removeFromIndex(CollectionRequest collectionRequest, Pair<String, String> keyValue);

    List<DBDocumentLocation> getByKey(String indexPath, String key);

    void setByKey(String indexPath, Pair<String, List<DBDocumentLocation>> pair);

    List<Pair<String, String>> findRegisteredIndexes(CollectionRequest request);
}
