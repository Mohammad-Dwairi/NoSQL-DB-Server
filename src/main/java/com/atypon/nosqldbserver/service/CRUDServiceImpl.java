package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.core.DBDocument;
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

import static com.atypon.nosqldbserver.utils.DBFilePath.buildDefaultIndexPath;
import static com.atypon.nosqldbserver.utils.DBFilePath.buildRequestedIndexPath;

@Service
@RequiredArgsConstructor
public class CRUDServiceImpl implements CRUDService {

    private final CollectionService collectionService;
    private final DocumentService documentService;
    private final IndexService indexService;

    @Override
    public List<DBDocument> findByDefaultId(CollectionId collectionId) {
        if (collectionService.find(collectionId).isPresent()) {
            String indexPath = buildDefaultIndexPath(collectionId);
            DBDefaultIndex defaultIndex = new DBDefaultIndex(indexPath);
            return documentService.findAll(collectionId, defaultIndex.values());
        }
        throw new CollectionNotFoundException();
    }

    @Override
    public List<DBDocument> findByIndexedProperty(DocumentId documentId) {
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
    public void save(CollectionId collectionId, Object document) {
        DBDocument dbDocument = new DBDocument(generateDefaultId(), document);
        DBDocumentLocation location = documentService.save(collectionId, dbDocument);
        indexService.save(collectionId, new Pair<>(dbDocument, location));
    }

    @Override
    public void updateByDefaultId(CollectionId collectionId, DBDocument dbDocument) {
        DBDefaultIndex defaultIndex = new DBDefaultIndex(buildDefaultIndexPath(collectionId));
        defaultIndex.get(dbDocument.getDefaultId()).orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        DBDocumentLocation updatedLocation = documentService.save(collectionId, dbDocument);
        indexService.update(collectionId, new Pair<>(dbDocument, updatedLocation));
    }

    @Override
    public void updateByIndexedProperty(DocumentId documentId, Object updatedDocument) {
        List<String> updatedPointers = extractRequestedData(documentId);
        updatedPointers.forEach(pointer -> {
            updateByDefaultId(documentId.getCollectionId(), new DBDocument(pointer, updatedDocument));
        });
    }

    @Override
    public void deleteByDefaultId(CollectionId collectionId, String defaultId) {
        indexService.drop(collectionId, defaultId);
    }

    @Override
    public void deleteByIndexedProperty(DocumentId documentId) {
        List<String> deletedPointers = extractRequestedData(documentId);
        deletedPointers.forEach(pointer -> deleteByDefaultId(documentId.getCollectionId(), pointer));
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
