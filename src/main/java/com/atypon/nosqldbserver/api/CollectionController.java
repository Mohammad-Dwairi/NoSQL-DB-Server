package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.core.DBCollection;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.service.collection.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/db/{schemaName}")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;
    private static final String COLLECTION_NAME = "collectionName";

    @GetMapping
    public List<DBCollection> findAllCollections(@PathVariable String schemaName) {
        return collectionService.findAll(schemaName);
    }

    @GetMapping(params = {"collection"})
    public DBCollection findAllCollections(@PathVariable String schemaName, @RequestParam String collection) {
        return collectionService.find(new CollectionId(schemaName, collection)).orElse(null);
    }

    @PostMapping
    public void createCollection(@PathVariable String schemaName, @RequestBody DBCollection collection) {
        collectionService.create(schemaName, collection);
    }

    @DeleteMapping
    public void dropCollection(@PathVariable String schemaName, @RequestBody Map<String, String> request) {
        if (request.containsKey(COLLECTION_NAME)) {
            CollectionId collectionId = new CollectionId(schemaName, request.get(COLLECTION_NAME));
            collectionService.drop(collectionId);
        }
    }

    @PostMapping("/index")
    public void createIndex(@PathVariable String schemaName, @RequestBody Map<String, String> request) {
        collectionService.createRequestedIndex(new CollectionId(schemaName, request.get("collectionName")), request.get("property"));
    }
}
