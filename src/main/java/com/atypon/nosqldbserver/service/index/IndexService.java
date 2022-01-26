package com.atypon.nosqldbserver.service.index;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.Pair;

import java.util.Map;

public interface IndexService {
    void save(CollectionId collectionId, Pair<Map<String, String>, DBDocumentLocation> documentLocationPair);
    void update(CollectionId collectionId, Pair<String, DBDocumentLocation> updates);
    void drop(CollectionId collectionId, String id);
}
