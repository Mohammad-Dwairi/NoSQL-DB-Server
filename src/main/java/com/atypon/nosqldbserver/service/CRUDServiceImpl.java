package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.exceptions.CollectionNotFoundException;
import com.atypon.nosqldbserver.exceptions.DocumentNotFoundException;
import com.atypon.nosqldbserver.index.DBDefaultIndex;
import com.atypon.nosqldbserver.index.DBRequestedIndex;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.IndexedDocument;
import com.atypon.nosqldbserver.helper.Pair;
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

import static com.atypon.nosqldbserver.utils.DBFilePath.getDefaultIndexPath;
import static com.atypon.nosqldbserver.utils.DBFilePath.getRequestedIndexPath;

@Service
@RequiredArgsConstructor
public class CRUDServiceImpl implements CRUDService {

    private final CollectionService collectionService;
    private final DocumentService documentService;
    private final IndexService indexService;

    @Override
    public List<DBDocument> findAllByDefaultIndex(CollectionId collectionId) {
        if (collectionService.find(collectionId).isPresent()) {
            String indexPath = getDefaultIndexPath(collectionId);
            DBDefaultIndex defaultIndex = new DBDefaultIndex(indexPath);
            return documentService.findAll(collectionId, defaultIndex.values());
        }
        throw new CollectionNotFoundException();
    }

    @Override
    public List<DBDocument> findByIndexedProperty(IndexedDocument indexedDocument) {
        final String defaultIndexPath = getDefaultIndexPath(indexedDocument.getCollectionId());
        List<String> pointers = extractRequestedData(indexedDocument);
        if (!pointers.isEmpty()) {
            final DBDefaultIndex defaultIndex = new DBDefaultIndex(defaultIndexPath);
            List<DBDocumentLocation> locations = defaultIndex.get(pointers);
            if (locations == null) {
                locations = new ArrayList<>();
            }
            return documentService.findAll(indexedDocument.getCollectionId(), locations);
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
        DBDefaultIndex defaultIndex = new DBDefaultIndex(getDefaultIndexPath(collectionId));
        defaultIndex.get(dbDocument.getDefaultId()).orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        DBDocumentLocation updatedLocation = documentService.save(collectionId, dbDocument);
        indexService.update(collectionId, new Pair<>(dbDocument, updatedLocation));
    }

    @Override
    public void updateByIndexedProperty(IndexedDocument indexedDocument, Object updatedDocument) {
        List<String> updatedPointers = extractRequestedData(indexedDocument);
        updatedPointers.forEach(pointer -> {
            updateByDefaultId(indexedDocument.getCollectionId(), new DBDocument(pointer, updatedDocument));
        });
    }

    @Override
    public void deleteByDefaultId(CollectionId collectionId, String defaultId) {
        indexService.drop(collectionId, defaultId);
    }

    @Override
    public void deleteByIndexedProperty(IndexedDocument indexedDocument) {
        List<String> deletedPointers = extractRequestedData(indexedDocument);
        deletedPointers.forEach(pointer -> deleteByDefaultId(indexedDocument.getCollectionId(), pointer));
    }

    private List<String> extractRequestedData(IndexedDocument indexedDocument) {
        final String indexedPropertyValue = indexedDocument.getIndexedPropertyValue();
        final CollectionId collectionId = indexedDocument.getCollectionId();
        final String requestedIndexPath = getRequestedIndexPath(collectionId, indexedDocument.getIndexedPropertyName());
        DBRequestedIndex requestedIndex = new DBRequestedIndex(requestedIndexPath);
        return requestedIndex.get(indexedPropertyValue).orElse(new ArrayList<>());
    }

    private String generateDefaultId() {
        return String.valueOf(Timestamp.from(Instant.now()).getTime());
    }

}
