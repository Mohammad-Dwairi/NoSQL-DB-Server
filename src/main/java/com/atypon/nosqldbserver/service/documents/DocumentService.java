package com.atypon.nosqldbserver.service.documents;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.DocumentId;

import java.util.List;
import java.util.Map;

public interface DocumentService {

    List<Map<String, String>> findAll(CollectionId collectionId, List<DBDocumentLocation> locations);
    Map<String, String> find(CollectionId collectionId, DBDocumentLocation location);
    DBDocumentLocation save(CollectionId collectionId, Map<String, String> document);
    List<DBDocumentLocation> saveAll(CollectionId collectionId, List<Map<String, String>> document);
    void delete(DocumentId documentId);
}
