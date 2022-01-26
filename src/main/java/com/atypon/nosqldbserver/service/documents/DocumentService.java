package com.atypon.nosqldbserver.service.documents;

import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.DocumentId;

import java.util.List;
import java.util.Map;

public interface DocumentService {
    List<DBDocument> findAll(CollectionId collectionId);
    List<DBDocument> findAll(CollectionId collectionId, List<DBDocumentLocation> locations);
    DBDocument find(CollectionId collectionId, DBDocumentLocation location);
    DBDocumentLocation save(CollectionId collectionId, DBDocument document);
}
