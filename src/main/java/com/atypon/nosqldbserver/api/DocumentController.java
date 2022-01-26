package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.IndexedDocument;
import com.atypon.nosqldbserver.service.CRUDService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/db/{schemaName}/{collectionName}")
@RequiredArgsConstructor
public class DocumentController {

    private final CRUDService crudService;

    @GetMapping(params = {"property", "value"})
    public List<DBDocument> findByIndexedKey(CollectionId collectionId, @RequestParam String property, @RequestParam String value) {
        IndexedDocument docReq = new IndexedDocument(collectionId, property, value);
        return crudService.findByIndexedProperty(docReq);
    }

    @GetMapping
    public List<DBDocument> findAll(CollectionId collectionId) {
        return crudService.findByDefaultId(collectionId);
    }

    @PostMapping
    public void save(CollectionId collectionId, @RequestBody Object document) {
        System.out.println(document);
        crudService.save(collectionId, document);
    }

    @PutMapping(params = {"property", "value"})
    public void update(CollectionId collectionId, @RequestParam String property, @RequestParam String value, @RequestBody Object updates) {
        IndexedDocument indexedDocument = new IndexedDocument(collectionId, property, value);
        crudService.updateByIndexedProperty(indexedDocument, updates);
    }

    @PutMapping("/{docId}")
    public void update(CollectionId collectionId, @RequestBody Object updates, @PathVariable String docId) {
        crudService.updateByDefaultId(collectionId, new DBDocument(docId, updates));
    }

    @DeleteMapping("/{docId}")
    public void delete(CollectionId collectionId, @PathVariable String docId) {
        crudService.deleteByDefaultId(collectionId, docId);
    }

    @DeleteMapping(params = {"property", "value"})
    public void delete(CollectionId colReq, @RequestParam String property, @RequestParam String value) {
        crudService.deleteByIndexedProperty(new IndexedDocument(colReq, property, value));
    }
}
