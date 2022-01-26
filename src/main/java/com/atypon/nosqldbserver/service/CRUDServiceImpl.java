package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.exceptions.CollectionNotFoundException;
import com.atypon.nosqldbserver.exceptions.DocumentNotFoundException;
import com.atypon.nosqldbserver.index.DBDefaultIndex;
import com.atypon.nosqldbserver.index.DBRequestedIndex;
import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.DocumentId;
import com.atypon.nosqldbserver.request.Pair;
import com.atypon.nosqldbserver.service.collection.CollectionService;
import com.atypon.nosqldbserver.service.documents.DocumentService;
import com.atypon.nosqldbserver.service.index.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atypon.nosqldbserver.utils.DBFilePath.buildDefaultIndexPath;
import static com.atypon.nosqldbserver.utils.DBFilePath.buildRequestedIndexPath;

@Service
@RequiredArgsConstructor
public class CRUDServiceImpl implements CRUDService {

    private final CollectionService collectionService;
    private final DocumentService documentService;
    private final IndexService indexService;

    @Override
    public List<Map<String, String>> findByDefaultId(CollectionId collectionId) {
        if (collectionService.find(collectionId).isPresent()) {
            String indexPath = buildDefaultIndexPath(collectionId);
            DBDefaultIndex defaultIndex = new DBDefaultIndex(indexPath);
            return documentService.findAll(collectionId, defaultIndex.values());
        }
        throw new CollectionNotFoundException();
    }

    @Override
    public List<Map<String, String>> findByIndexedProperty(DocumentId documentId) {
        final String defaultIndexPath = buildDefaultIndexPath(documentId.getCollectionId());
        List<String> pointers = extractRequestedData(documentId);
        if (!pointers.isEmpty()) {
            final DBDefaultIndex defaultIndex = new DBDefaultIndex(defaultIndexPath);
            List<DBDocumentLocation> locations = defaultIndex.get(pointers);
            if (locations == null) {
                locations = new ArrayList<>();
            }
            return documentService.findAll(documentId.getCollectionId(), locations);
        }
        return Collections.emptyList();
    }

    @Override
    public void save(CollectionId collectionId, Map<String, String> document) {
        document.put("_$id", generateDefaultId());
        DBDocumentLocation location = documentService.save(collectionId, document);
        indexService.save(collectionId, new Pair<>(document, location));
    }

    @Override
    public void updateByDefaultId(DocumentId documentId, Map<String, String> updates) {
        updates.remove("_$id");
        final String defaultId = documentId.getIndexedPropertyValue();
        final CollectionId collectionId = documentId.getCollectionId();
        DBDefaultIndex defaultIndex = new DBDefaultIndex(buildDefaultIndexPath(collectionId));
        DBDocumentLocation originalLocation = defaultIndex.get(defaultId).orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        Map<String, String> document = documentService.find(collectionId, originalLocation);
        document.putAll(updates);
        DBDocumentLocation updatedLocation = documentService.save(collectionId, document);
        indexService.update(collectionId, new Pair<>(defaultId, updatedLocation));
    }

    @Override
    public void updateByIndexedProperty(DocumentId documentId, Map<String, String> updates) {
        updates.remove("_$id");
        List<String> updatedPointers = extractRequestedData(documentId);
        updatedPointers.forEach(pointer -> {
            updateByDefaultId(new DocumentId(documentId.getCollectionId(), "_$id", pointer), updates);
        });
    }

    @Override
    public void deleteByDefaultId(DocumentId documentId) {
        final String defaultId = documentId.getIndexedPropertyValue();
        final CollectionId collectionId = documentId.getCollectionId();
        indexService.drop(collectionId, defaultId);
    }

    @Override
    public void deleteByIndexedProperty(DocumentId documentId) {
        List<String> deletedPointers = extractRequestedData(documentId);
        deletedPointers.forEach(pointer -> deleteByDefaultId(new DocumentId(documentId.getCollectionId(), "_$id", pointer)));
    }

    private List<String> extractRequestedData(DocumentId documentId) {
        final String indexedPropertyValue = documentId.getIndexedPropertyValue();
        final CollectionId collectionId = documentId.getCollectionId();
        final String requestedIndexPath = buildRequestedIndexPath(collectionId, documentId.getIndexedPropertyName());
        DBRequestedIndex requestedIndex = new DBRequestedIndex(requestedIndexPath);
        return requestedIndex.get(indexedPropertyValue).orElse(new ArrayList<>());
    }

    private String generateDefaultId() {
        return String.valueOf(Timestamp.from(Instant.now()).getTime());
    }

}
