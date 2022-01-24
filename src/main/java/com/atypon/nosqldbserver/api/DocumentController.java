package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.DocumentId;
import com.atypon.nosqldbserver.service.CRUDService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/db/{schemaName}/{collectionName}")
@RequiredArgsConstructor
public class DocumentController {

    private final CRUDService crudService;

    @GetMapping(params = {"property", "value"})
    public List<Map<String, String>> findByIndexedKey(CollectionId collectionId, @RequestParam String property, @RequestParam String value) {
        DocumentId docReq = new DocumentId(collectionId, property, value);
        return crudService.find(docReq);
    }

    @GetMapping
    public List<Map<String, String>> findAll(CollectionId request) {
        return crudService.findAll(request);
    }

    @PostMapping
    public void save(CollectionId colReq, @RequestBody Map<String, String> document) {
        crudService.save(colReq, document);
    }

    @PutMapping(params = {"property", "value"})
    public void update(CollectionId collectionId, @RequestParam String property, @RequestParam String value, @RequestBody Map<String, String> updates) {
        DocumentId req = new DocumentId(collectionId, property, value);
        crudService.updateByIndexedProperty(req, updates);
    }

    @PutMapping("/{docId}")
    public void update(CollectionId collectionId, @RequestBody Map<String, String> updates, @PathVariable String docId) {
        DocumentId req = new DocumentId(collectionId, "_$id", docId);
        crudService.update(req, updates);
    }

    @DeleteMapping("/{docId}")
    public void delete(CollectionId colReq, @PathVariable String docId) {
        crudService.delete(new DocumentId(colReq, "_$id", docId));
    }

    @DeleteMapping(params = {"property", "value"})
    public void delete(CollectionId colReq, @RequestParam String property, @RequestParam String value) {
        crudService.deleteByIndexedProperty(new DocumentId(colReq, property, value));
    }
}
