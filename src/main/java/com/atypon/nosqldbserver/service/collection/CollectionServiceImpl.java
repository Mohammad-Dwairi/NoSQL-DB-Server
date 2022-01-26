package com.atypon.nosqldbserver.service.collection;

import com.atypon.nosqldbserver.access.DBFileAccess;
import com.atypon.nosqldbserver.access.DBFileAccessPool;
import com.atypon.nosqldbserver.core.DBCollection;
import com.atypon.nosqldbserver.core.DBSchema;
import com.atypon.nosqldbserver.exceptions.CollectionAlreadyExistsException;
import com.atypon.nosqldbserver.exceptions.CollectionNotFoundException;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.atypon.nosqldbserver.exceptions.SchemaNotFoundException;
import com.atypon.nosqldbserver.index.DBDefaultIndex;
import com.atypon.nosqldbserver.index.DBRequestedIndex;
import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.Pair;
import com.atypon.nosqldbserver.service.documents.DocumentService;
import com.atypon.nosqldbserver.service.file.FileService;
import com.atypon.nosqldbserver.service.schema.SchemaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.atypon.nosqldbserver.utils.DBFilePath.*;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final DocumentService documentService;
    private final SchemaService schemaService;
    private final FileService fileService;

    @Override
    public List<DBCollection> findAll(String schemaName) {
        return schemaService.find(schemaName).orElseThrow(SchemaNotFoundException::new).getCollections();
    }

    @Override
    public Optional<DBCollection> find(CollectionId collectionId) {
        return findAll(collectionId.getSchemaName()).stream()
                .filter(c -> c.getName().equals(collectionId.getCollectionName())).findFirst();
    }

    @Override
    public void create(String schemaName, DBCollection collection) {
        CollectionId collectionId = new CollectionId(schemaName, collection.getName());
        if (find(collectionId).isPresent()) {
            throw new CollectionAlreadyExistsException();
        }
        List<DBSchema> schemas = schemaService.findAll();
        DBSchema schema = schemas.stream().filter(s -> s.getName().equals(schemaName))
                .findAny().orElseThrow(SchemaNotFoundException::new);
        schema.addCollection(collection);
        fileService.createFile(buildCollectionPath(collectionId));
        fileService.createFile(buildDefaultIndexPath(collectionId));
        schemaService.writeToSchemaFile(schemas);
    }

    @Override
    public void drop(CollectionId collectionId) {
        List<DBSchema> schemas = schemaService.findAll();
        DBSchema parentSchema = schemaService.find(collectionId.getSchemaName()).orElseThrow(SchemaNotFoundException::new);
        int parentSchemaIndex = schemas.indexOf(parentSchema);
        boolean removed = parentSchema.dropCollection(collectionId.getCollectionName());
        if (removed) {
            schemas.set(parentSchemaIndex, parentSchema);
            schemaService.writeToSchemaFile(schemas);
            fileService.deleteFile(buildCollectionPath(collectionId));
        } else {
            throw new CollectionNotFoundException();
        }
    }

    @Override
    public void createRequestedIndex(CollectionId collectionId, String indexedPropertyName) {
        try {
            if (find(collectionId).isPresent()) {
                String requestedIndexPath = createRequestedIndexFile(collectionId, indexedPropertyName);
                String registeredIndexesFile = createRegisteredIndexesFile(collectionId);
                DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(registeredIndexesFile);
                String registeredIndexesJSON = fileAccess.read();
                List<Pair<String, String>> registeredIndexes = new ArrayList<>();
                if (!registeredIndexesJSON.isBlank()) {
                    registeredIndexes = new ObjectMapper().readValue(registeredIndexesJSON, new TypeReference<>() {
                    });
                }
                registeredIndexes.add(new Pair<>(indexedPropertyName, requestedIndexPath));
                fileAccess.clear();
                fileAccess.write(convertToJSON(registeredIndexes));
                recoverExistingDocuments(collectionId, indexedPropertyName);
            }
        } catch (IOException e) {
            throw new JSONParseException(e.getMessage());
        }
    }

    @Override
    public List<Pair<String, String>> getRegisteredIndexes(CollectionId collectionId) {
        final String registeredIndexesFile = createRegisteredIndexesFile(collectionId);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(registeredIndexesFile);
        String indexesJSON = fileAccess.read();
        if (!indexesJSON.isBlank()) {
            try {
                return new ObjectMapper().readValue(indexesJSON, new TypeReference<>() {
                });
            } catch (IOException e) {
                throw new JSONParseException(e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void recoverExistingDocuments(CollectionId collectionId, String indexedPropertyName) {
        final String defaultIndexPath = buildDefaultIndexPath(collectionId);
        final String requestedIndexPath = buildRequestedIndexPath(collectionId, indexedPropertyName);
        DBDefaultIndex defaultIndex = new DBDefaultIndex(defaultIndexPath);
        DBRequestedIndex requestedIndex = new DBRequestedIndex(requestedIndexPath);
        requestedIndex.clear();
        List<Map<String, String>> docs = documentService.findAll(collectionId, defaultIndex.values());
        docs.forEach(doc -> requestedIndex.add(doc.get(indexedPropertyName), doc.get("_$id")));
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(requestedIndexPath);
        fileAccess.clear();
        fileAccess.write(requestedIndex.toJSON());
    }

    private String createRequestedIndexFile(CollectionId collectionId, String indexedPropertyName) {
        final String path = buildRequestedIndexPath(collectionId, indexedPropertyName);
        fileService.createFile(path);
        return path;
    }

    private String createRegisteredIndexesFile(CollectionId collectionId) {
        final String path = buildIndexesFilePath(collectionId);
        fileService.createFile(path);
        return path;
    }


}
