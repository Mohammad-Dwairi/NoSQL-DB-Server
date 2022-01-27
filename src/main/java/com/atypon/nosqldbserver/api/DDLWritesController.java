package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.core.DBCollection;
import com.atypon.nosqldbserver.core.DBSchema;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.service.collection.CollectionService;
import com.atypon.nosqldbserver.service.schema.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Profile("master")
@RequestMapping("/db/ddl-write")
@RequiredArgsConstructor
public class DDLWritesController {

    private final SchemaService schemaService;
    private final CollectionService collectionService;


    @PostMapping("/schema")
    public void createSchema(@RequestBody Map<String, String> request) {
        if (request.containsKey("schemaName")) {
            schemaService.create(request.get("schemaName"));
        }
    }

    @PostMapping("/schema/import")
    public void importSchema(@RequestBody DBSchema schema) {
        schemaService.importSchema(schema);
    }

    @DeleteMapping("/schema/{schemaName}")
    public void dropSchema(@PathVariable String schemaName) {
        schemaService.drop(schemaName);
    }

    @PostMapping("/schema/{schemaName}")
    public void createCollection(@PathVariable String schemaName, @RequestBody DBCollection collection) {
        collectionService.create(schemaName, collection);
    }

    @DeleteMapping("/schema/{schemaName}/{collectionName}")
    public void dropCollection(CollectionId collectionId) {
        collectionService.drop(collectionId);
    }

    @PostMapping("/schema/{schemaName}/{collectionName}/{propertyName}/index")
    public void createIndex(CollectionId collectionId, @PathVariable String propertyName) {
        collectionService.createRequestedIndex(collectionId, propertyName);
    }



}
