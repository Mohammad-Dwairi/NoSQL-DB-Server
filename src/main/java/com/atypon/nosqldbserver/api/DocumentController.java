package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.request.CollectionRequest;
import com.atypon.nosqldbserver.request.DocumentRequest;
import com.atypon.nosqldbserver.request.Pair;
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
    public List<Map<String, String>> findByIndexedKey(CollectionRequest collectionRequest, @RequestParam String property, @RequestParam String value) {
        DocumentRequest docReq = new DocumentRequest(collectionRequest, property, value);
        return crudService.find(docReq);
    }

    @GetMapping
    public List<Map<String, String>> findAll(CollectionRequest request) {
        return crudService.findAll(request);
    }

    @PostMapping
    public void save(CollectionRequest colReq, @RequestBody Map<String, String> document) {
        crudService.save(colReq, document);
    }

    @PutMapping
    public void update(CollectionRequest collectionRequest, @RequestParam String property, @RequestParam String value, @RequestBody Map<String, String> updates) {
        DocumentRequest req = new DocumentRequest(collectionRequest, property, value);
        crudService.update(req, updates);
    }

    @DeleteMapping
    public void delete(CollectionRequest colReq, @RequestParam String property, @RequestParam String value) {
        crudService.delete(new DocumentRequest(colReq, property, value));
    }
}
