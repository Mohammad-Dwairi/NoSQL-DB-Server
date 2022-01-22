package com.atypon.nosqldbserver.service.collection;

import com.atypon.nosqldbserver.core.DBCollection;
import com.atypon.nosqldbserver.core.DBSchema;
import com.atypon.nosqldbserver.exceptions.CollectionAlreadyExistsException;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.atypon.nosqldbserver.exceptions.SchemaNotFoundException;
import com.atypon.nosqldbserver.request.CollectionRequest;
import com.atypon.nosqldbserver.request.Pair;
import com.atypon.nosqldbserver.service.file.FileService;
import com.atypon.nosqldbserver.service.index.IndexService;
import com.atypon.nosqldbserver.service.schema.SchemaService;
import com.atypon.nosqldbserver.utils.DBFileReader;
import com.atypon.nosqldbserver.utils.DBFileWriter;
import com.atypon.nosqldbserver.utils.JSONUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.atypon.nosqldbserver.utils.DBFilePath.buildCollectionPath;
import static com.atypon.nosqldbserver.utils.DBFilePath.buildIndexPath;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final IndexService indexService;
    private final SchemaService schemaService;
    private final FileService fileService;

    @Override
    public List<DBCollection> findAll(String schemaName) {
        return schemaService.find(schemaName).orElseThrow(SchemaNotFoundException::new).getCollections();
    }

    @Override
    public Optional<DBCollection> find(CollectionRequest collectionRequest) {
        return findAll(collectionRequest.getSchemaName()).stream()
                .filter(c -> c.getName().equals(collectionRequest.getCollectionName())).findFirst();
    }

    @Override
    public void create(String schemaName, DBCollection collection) {
        CollectionRequest collectionRequest = new CollectionRequest(schemaName, collection.getName());
        if (find(collectionRequest).isPresent()) {
            throw new CollectionAlreadyExistsException();
        }
        List<DBSchema> schemas = schemaService.findAll();
        DBSchema schema = schemas.stream().filter(s -> s.getName().equals(schemaName))
                .findAny().orElseThrow(SchemaNotFoundException::new);
        schema.getCollections().add(collection);
        fileService.createFile(buildCollectionPath(collectionRequest));
        schemaService.writeToSchemaFile(schemas);
        indexService.createIndex("__id__", collectionRequest); //side effect
    }

    @Override
    public void drop(CollectionRequest collectionRequest) {
        List<DBSchema> schemas = schemaService.findAll();
        schemas.stream().filter(s -> s.getName().equals(collectionRequest.getSchemaName())).findFirst()
                .ifPresent(s -> s.getCollections().removeIf(c -> c.getName().equals(collectionRequest.getCollectionName())));
        schemaService.writeToSchemaFile(schemas);
        fileService.deleteFile(buildCollectionPath(collectionRequest));
    }

    @Override
    public void createIndex(CollectionRequest colReq, String key) {
        indexService.createIndex(key, colReq);
    }
}
