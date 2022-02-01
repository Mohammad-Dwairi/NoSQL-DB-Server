package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.IndexedDocument;
import com.atypon.nosqldbserver.service.CRUDService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;


@RestController
@Profile("master")
@RequestMapping("/db/dml-write")
@RequiredArgsConstructor
public class DMLWritesController {

    private final CRUDService crudService;

    @PostMapping("/schema/{schemaName}/{collectionName}")
    public DBDocument save(CollectionId collectionId, @RequestBody Object document) {
        return crudService.save(collectionId, document);
    }

    @PutMapping(value = "/schema/{schemaName}/{collectionName}", params = {"property", "value"})
    public void update(CollectionId collectionId, @RequestParam String property, @RequestParam String value, @RequestBody Object updates) {
        IndexedDocument indexedDocument = new IndexedDocument(collectionId, property, value);
        crudService.updateByIndexedProperty(indexedDocument, updates);
    }

    @PutMapping("/schema/{schemaName}/{collectionName}/{docId}")
    public void update(CollectionId collectionId, @RequestBody Object updates, @PathVariable String docId) {
        IndexedDocument indexedDocument = new IndexedDocument(collectionId, "defaultId", docId);
        crudService.updateByDefaultId(indexedDocument, updates);
    }

    @DeleteMapping("/schema/{schemaName}/{collectionName}/{docId}")
    public void delete(CollectionId collectionId, @PathVariable String docId) {
        IndexedDocument indexedDocument = IndexedDocument.builder()
                .collectionId(collectionId)
                .indexedPropertyName("defaultId")
                .indexedPropertyValue(docId)
                .build();
        crudService.deleteByDefaultId(indexedDocument);
    }

    @DeleteMapping(value = "/schema/{schemaName}/{collectionName}", params = {"property", "value"})
    public void delete(CollectionId collectionId, @RequestParam String property, @RequestParam String value) {
        crudService.deleteByIndexedProperty(new IndexedDocument(collectionId, property, value));
    }
}
