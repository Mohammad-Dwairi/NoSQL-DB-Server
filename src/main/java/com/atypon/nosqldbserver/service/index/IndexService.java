package com.atypon.nosqldbserver.service.index;

import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.Pair;

public interface IndexService {
    void save(CollectionId collectionId, Pair<DBDocument, DBDocumentLocation> documentLocationPair);
    void update(CollectionId collectionId, Pair<DBDocument, DBDocumentLocation> updates);
    void drop(CollectionId collectionId, String id);
}
