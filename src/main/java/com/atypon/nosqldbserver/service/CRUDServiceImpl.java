package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.exceptions.CollectionNotFoundException;
import com.atypon.nosqldbserver.exceptions.DocumentNotFoundException;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.IndexedDocument;
import com.atypon.nosqldbserver.helper.Pair;
import com.atypon.nosqldbserver.index.DBDefaultIndex;
import com.atypon.nosqldbserver.index.DBRequestedIndex;
import com.atypon.nosqldbserver.service.collection.CollectionService;
import com.atypon.nosqldbserver.service.documents.DocumentService;
import com.atypon.nosqldbserver.service.index.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.atypon.nosqldbserver.utils.DBFilePath.getDefaultIndexPath;
import static com.atypon.nosqldbserver.utils.DBFilePath.getRequestedIndexPath;

@Service
@RequiredArgsConstructor
public class CRUDServiceImpl implements CRUDService {
    private static int ID_SEQUENCE = 0;
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
    public List<DBDocument> findByDefaultId(IndexedDocument indexedDocument) {
        CollectionId collectionId = indexedDocument.getCollectionId();
        DBDefaultIndex index = new DBDefaultIndex(getDefaultIndexPath(collectionId));
        Optional<DBDocumentLocation> locationOptional = index.get(indexedDocument.getIndexedPropertyValue());
        return locationOptional.map(location -> List.of(documentService.find(collectionId, location))).orElse(Collections.emptyList());
    }

    @Override
    public DBDocument save(CollectionId collectionId, Object document) {
        DBDocument dbDocument = DBDocument.create(Integer.toString(ID_SEQUENCE++), document);
        DBDocumentLocation location = documentService.save(collectionId, dbDocument);
        indexService.save(collectionId, new Pair<>(dbDocument, location));
        return dbDocument;
    }

    @Override
    public void updateByDefaultId(IndexedDocument indexedDocument, Object updated) {
        CollectionId collectionId = indexedDocument.getCollectionId();
        final String defaultId = indexedDocument.getIndexedPropertyValue();
        DBDefaultIndex defaultIndex = new DBDefaultIndex(getDefaultIndexPath(collectionId));
        defaultIndex.get(defaultId).orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        DBDocument updatedDocument = DBDocument.create(defaultId, updated);
        DBDocumentLocation updatedLocation = documentService.save(collectionId, updatedDocument);
        indexService.update(collectionId, new Pair<>(updatedDocument, updatedLocation));
    }

    @Override
    public void updateByIndexedProperty(IndexedDocument indexedDocument, Object updatedDocument) {
        List<String> updatedPointers = extractRequestedData(indexedDocument);
        IndexedDocument defaultIndexedDocument = IndexedDocument.builder()
                .collectionId(indexedDocument.getCollectionId())
                .indexedPropertyName("defaultId")
                .build();
        updatedPointers.forEach(pointer -> {
            defaultIndexedDocument.setIndexedPropertyValue(pointer);
            updateByDefaultId(defaultIndexedDocument, updatedDocument);
        });
    }

    @Override
    public void deleteByDefaultId(IndexedDocument indexedDocument) {
        CollectionId collectionId = indexedDocument.getCollectionId();
        String defaultId = indexedDocument.getIndexedPropertyValue();
        indexService.drop(collectionId, defaultId);
    }

    @Override
    public void deleteByIndexedProperty(IndexedDocument indexedDocument) {
        List<String> deletedPointers = extractRequestedData(indexedDocument);
        IndexedDocument defaultIndexedDocument = IndexedDocument.builder()
                .collectionId(indexedDocument.getCollectionId())
                .indexedPropertyName("defaultId")
                .build();
        deletedPointers.forEach(pointer -> {
            defaultIndexedDocument.setIndexedPropertyValue(pointer);
            deleteByDefaultId(defaultIndexedDocument);
        });
    }

    private List<String> extractRequestedData(IndexedDocument indexedDocument) {
        final String indexedPropertyValue = indexedDocument.getIndexedPropertyValue();
        final CollectionId collectionId = indexedDocument.getCollectionId();
        final String requestedIndexPath = getRequestedIndexPath(collectionId, indexedDocument.getIndexedPropertyName());
        DBRequestedIndex requestedIndex = new DBRequestedIndex(requestedIndexPath);
        return requestedIndex.get(indexedPropertyValue).orElse(new ArrayList<>());
    }

}
