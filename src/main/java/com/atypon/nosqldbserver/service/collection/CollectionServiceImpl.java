package com.atypon.nosqldbserver.service.collection;

import com.atypon.nosqldbserver.access.DBFileAccess;
import com.atypon.nosqldbserver.access.DBFileAccessPool;
import com.atypon.nosqldbserver.core.DBCollection;
import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.core.DBSchema;
import com.atypon.nosqldbserver.exceptions.*;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.Pair;
import com.atypon.nosqldbserver.index.DBDefaultIndex;
import com.atypon.nosqldbserver.index.DBIndex;
import com.atypon.nosqldbserver.index.DBRequestedIndex;
import com.atypon.nosqldbserver.service.documents.DocumentService;
import com.atypon.nosqldbserver.service.file.FileService;
import com.atypon.nosqldbserver.service.schema.SchemaService;
import com.atypon.nosqldbserver.utils.JSONUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.atypon.nosqldbserver.utils.DBFilePath.*;
import static com.atypon.nosqldbserver.utils.JSONUtils.*;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToObjectMap;

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
        if (find(collectionId).isEmpty()) {
            List<DBSchema> schemas = schemaService.findAll();
            DBSchema schema = schemas.stream().filter(s -> s.getName().equals(schemaName))
                    .findAny().orElseThrow(SchemaNotFoundException::new);
            validateCollectionSchema(collection.getSchema());
            schema.addCollection(collection);
            fileService.createFolders(getCollectionDirPath(collectionId));
            fileService.createFile(getCollectionFilePath(collectionId));
            fileService.createFile(getDefaultIndexPath(collectionId));
            schemaService.writeToSchemaFile(schemas);
        } else {
            throw new CollectionAlreadyExistsException();
        }
    }

    @Override
    public void drop(CollectionId collectionId) {
        checkCollection(collectionId);
        List<DBSchema> schemas = schemaService.findAll();
        DBSchema parentSchema = schemaService.find(collectionId.getSchemaName()).orElseThrow(SchemaNotFoundException::new);
        int parentSchemaIndex = schemas.indexOf(parentSchema);
        boolean removed = parentSchema.dropCollection(collectionId.getCollectionName());
        if (removed) {
            schemas.set(parentSchemaIndex, parentSchema);
            schemaService.writeToSchemaFile(schemas);
            fileService.deleteFolders(getCollectionDirPath(collectionId));
        }
    }

    @Override
    public void createRequestedIndex(CollectionId collectionId, String indexedPropertyName) {
        checkCollection(collectionId);
        String requestedIndexPath = createRequestedIndexFile(collectionId, indexedPropertyName);
        List<Pair<String, String>> registeredIndexes = getRegisteredIndexes(collectionId);
        registeredIndexes.add(new Pair<>(indexedPropertyName, requestedIndexPath));
        writeToRegisteredIndexesFile(collectionId, registeredIndexes);
        recoverExistingDocuments(collectionId, indexedPropertyName);
    }

    @Override
    public List<Pair<String, String>> getRegisteredIndexes(CollectionId collectionId) {
        return tryReadRegisteredIndex(collectionId);
    }

    @Override
    public void recoverExistingDocuments(CollectionId collectionId, String indexedPropertyName) {
        final String defaultIndexPath = getDefaultIndexPath(collectionId);
        final String requestedIndexPath = getRequestedIndexPath(collectionId, indexedPropertyName);
        DBDefaultIndex defaultIndex = new DBDefaultIndex(defaultIndexPath);
        DBRequestedIndex requestedIndex = new DBRequestedIndex(requestedIndexPath);
        requestedIndex.clear();
        List<DBDocument> docs = documentService.findAll(collectionId, defaultIndex.values());
        docs.forEach(doc -> {
            Map<String, Object> docMap = convertToObjectMap(convertToJSON(doc.getDocument()));
            requestedIndex.add(docMap.get(indexedPropertyName), doc.getDefaultId());
        });
        writeToIndexFile(requestedIndex);
    }

    private String createRequestedIndexFile(CollectionId collectionId, String indexedPropertyName) {
        final String path = getRequestedIndexPath(collectionId, indexedPropertyName);
        fileService.createFile(path);
        return path;
    }

    private String createRegisteredIndexesFile(CollectionId collectionId) {
        final String path = getIndexesFilePath(collectionId);
        fileService.createFile(path);
        return path;
    }

    private List<Pair<String, String>> tryReadRegisteredIndex(CollectionId collectionId) {
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
        return new ArrayList<>();
    }


    private void checkCollection(CollectionId collectionId) {
        if (find(collectionId).isEmpty()) {
            throw new CollectionNotFoundException();
        }
    }

    private void writeToRegisteredIndexesFile(CollectionId collectionId, List<Pair<String, String>> newContent) {
        String registeredIndexesFile = createRegisteredIndexesFile(collectionId);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(registeredIndexesFile);
        fileAccess.clear();
        fileAccess.write(convertToJSON(newContent));
    }

    private void writeToIndexFile(DBIndex<?, ?> index) {
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(index.getPath());
        fileAccess.clear();
        fileAccess.write(index.toJSON());
    }

    private void validateCollectionSchema(Object collectionSchema) {
        Map<String, Object> objectMap = convertToObjectMap(convertToJSON(collectionSchema));
        boolean missingPropertiesKey = !objectMap.containsKey("properties");
        if (missingPropertiesKey) {
            throw new JSONSchemaValidationException("Collection schema must have 'properties' key");
        }
    }


}
