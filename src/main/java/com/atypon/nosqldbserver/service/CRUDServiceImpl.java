package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.exceptions.CollectionNotFoundException;
import com.atypon.nosqldbserver.exceptions.DocumentNotFoundException;
import com.atypon.nosqldbserver.index.DBDefaultIndex;
import com.atypon.nosqldbserver.index.DBRequestedIndex;
import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.DocumentId;
import com.atypon.nosqldbserver.service.collection.CollectionService;
import com.atypon.nosqldbserver.service.documents.DocumentService;
import com.atypon.nosqldbserver.utils.DBFileWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static com.atypon.nosqldbserver.utils.DBFilePath.buildDefaultIndexPath;
import static com.atypon.nosqldbserver.utils.DBFilePath.buildRequestedIndexPath;

@Service
@RequiredArgsConstructor
public class CRUDServiceImpl implements CRUDService {

    private final CollectionService collectionService;
    private final DocumentService documentService;


    @Override
    public List<Map<String, String>> findAll(CollectionId collectionId) {
        if (collectionService.find(collectionId).isPresent()) {
            String indexPath = buildDefaultIndexPath(collectionId);
            DBDefaultIndex defaultIndex = new DBDefaultIndex(indexPath);
            return documentService.findAll(collectionId, defaultIndex.values());
        }
        throw new CollectionNotFoundException();
    }

    @Override
    public List<Map<String, String>> find(DocumentId documentId) {
        final String requestedIndexPath = buildRequestedIndexPath(documentId.getCollectionId(), documentId.getIndexedPropertyName());
        final String defaultIndexPath = buildDefaultIndexPath(documentId.getCollectionId());
        final DBRequestedIndex requestedIndex = new DBRequestedIndex(requestedIndexPath);
        Optional<List<String>> pointers = requestedIndex.get(documentId.getIndexedPropertyValue());
        if (pointers.isPresent()) {
            final DBDefaultIndex defaultIndex = new DBDefaultIndex(defaultIndexPath);
            List<DBDocumentLocation> locations = defaultIndex.get(pointers.get());
            if (locations == null) {
                locations = new ArrayList<>();
            }
            return documentService.findAll(documentId.getCollectionId(), locations);
        }
        return Collections.emptyList();
    }

    @Override
    public void save(CollectionId collectionId, Map<String, String> document) {
        final String defaultId = generateDefaultId();
        document.put("_$id", defaultId);
        DBDocumentLocation location = documentService.save(collectionId, document);
        DBDefaultIndex defaultIndex = new DBDefaultIndex(buildDefaultIndexPath(collectionId));
        defaultIndex.put(defaultId, location);
        DBFileWriter.clearAndWrite(defaultIndex.toJSON(), defaultIndex.getPath());
        collectionService.getRegisteredIndexes(collectionId).forEach(registeredIndex -> {
            DBRequestedIndex requestedIndex = new DBRequestedIndex(registeredIndex.getValue());
            final String key = document.get(registeredIndex.getKey());
            requestedIndex.add(key, defaultId);
            DBFileWriter.clearAndWrite(requestedIndex.toJSON(), registeredIndex.getValue());
        });
    }

    @Override
    public void update(DocumentId documentId, Map<String, String> updates) {
        updates.remove("_$id");
        final String defaultId = documentId.getIndexedPropertyValue();
        final CollectionId collectionId = documentId.getCollectionId();
        DBDefaultIndex defaultIndex = new DBDefaultIndex(buildDefaultIndexPath(collectionId));
        DBDocumentLocation originalLocation = defaultIndex.get(defaultId).orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        Map<String, String> document = documentService.find(collectionId, originalLocation);
        document.putAll(updates);
        DBDocumentLocation updatedLocation = documentService.save(collectionId, document);
        defaultIndex.put(defaultId, updatedLocation);
        DBFileWriter.clearAndWrite(defaultIndex.toJSON(), defaultIndex.getPath());
        collectionService.getRegisteredIndexes(documentId.getCollectionId()).forEach(registeredIndex -> {
            collectionService.recoverExistingDocuments(collectionId, registeredIndex.getKey());
        });
    }

    @Override
    public void updateByIndexedProperty(DocumentId documentId, Map<String, String> updates) {
        updates.remove("_$id");
        final String indexedPropertyValue = documentId.getIndexedPropertyValue();
        final CollectionId collectionId = documentId.getCollectionId();
        final String requestedIndexPath = buildRequestedIndexPath(collectionId, documentId.getIndexedPropertyName());
        DBRequestedIndex requestedIndex = new DBRequestedIndex(requestedIndexPath);
        List<String> updatedPointers = requestedIndex.get(indexedPropertyValue).orElse(new ArrayList<>());
        updatedPointers.forEach(pointer -> {
            update(new DocumentId(collectionId, "_$id", pointer), updates);
        });
    }

    @Override
    public void delete(DocumentId documentId) {
        final String defaultId = documentId.getIndexedPropertyValue();
        final CollectionId collectionId = documentId.getCollectionId();
        DBDefaultIndex defaultIndex = new DBDefaultIndex(buildDefaultIndexPath(collectionId));
        defaultIndex.drop(defaultId);
        DBFileWriter.clearAndWrite(defaultIndex.toJSON(), defaultIndex.getPath());
        collectionService.getRegisteredIndexes(documentId.getCollectionId()).forEach(registeredIndex -> {

        });
    }

    private String generateDefaultId() {
        return String.valueOf(Timestamp.from(Instant.now()).getTime());
    }

}
