package com.atypon.nosqldbserver.service.documents;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.request.CollectionRequest;
import com.atypon.nosqldbserver.request.DocumentRequest;

import java.util.List;
import java.util.Map;

public interface DocumentService {

    List<Map<String, String>> findAll(CollectionRequest collectionRequest, List<DBDocumentLocation> locations);

    DBDocumentLocation save(CollectionRequest collectionRequest, Map<String, String> document);
    List<DBDocumentLocation> saveAll(CollectionRequest collectionRequest, List<Map<String, String>> document);
    void delete(DocumentRequest documentRequest);
}
