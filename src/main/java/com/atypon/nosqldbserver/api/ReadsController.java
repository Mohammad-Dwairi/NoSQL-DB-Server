package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.core.DBCollection;
import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.core.DBSchema;
import com.atypon.nosqldbserver.exceptions.CollectionNotFoundException;
import com.atypon.nosqldbserver.exceptions.SchemaNotFoundException;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.IndexedDocument;
import com.atypon.nosqldbserver.helper.Pair;
import com.atypon.nosqldbserver.service.CRUDService;
import com.atypon.nosqldbserver.service.collection.CollectionService;
import com.atypon.nosqldbserver.service.schema.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.util.InMemoryResource;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/db/read")
@RequiredArgsConstructor
public class ReadsController {

    private final SchemaService schemaService;
    private final CollectionService collectionService;
    private final CRUDService crudService;

    @GetMapping("/schema")
    public List<DBSchema> findAllSchemas() {
        return schemaService.findAll();
    }

    @GetMapping("/schema/{schemaName}")
    public DBSchema findSchema(@PathVariable String schemaName) {
        return schemaService.find(schemaName).orElseThrow(SchemaNotFoundException::new);
    }

    @GetMapping("/schema/{schemaName}/export")
    public ResponseEntity<?> exportSchema(@PathVariable String schemaName, HttpServletResponse response) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + schemaName + ".json\"")
                .body(schemaService.find(schemaName).orElseThrow(SchemaNotFoundException::new));
    }

    @GetMapping("/schema/{schemaName}/collections")
    public List<DBCollection> findAllCollections(@PathVariable String schemaName) {
        return collectionService.findAll(schemaName);
    }

    @GetMapping("/schema/{schemaName}/collections/{collectionName}")
    public DBCollection findCollection(CollectionId collectionId) {
        return collectionService.find(collectionId).orElseThrow(CollectionNotFoundException::new);
    }

    @GetMapping("/schema/{schemaName}/{collectionName}/indexes")
    public List<String> findCollectionRegisteredIndexes(CollectionId collectionId) {
        return collectionService.getRegisteredIndexes(collectionId).stream().map(Pair::getKey).collect(Collectors.toList());
    }

    @GetMapping("/schema/{schemaName}/{collectionName}")
    public List<DBDocument> findAllDocumentsByDefaultIndex(CollectionId collectionId) {
        return crudService.findAllByDefaultIndex(collectionId);
    }

    @GetMapping("/schema/{schemaName}/{collectionName}/{docId}")
    public List<DBDocument> findDocumentByDefaultId(CollectionId collectionId, @PathVariable String docId) {
        IndexedDocument indexedDocument = new IndexedDocument(collectionId, "defaultId", docId);
        return crudService.findByDefaultId(indexedDocument);
    }

    @GetMapping(value = "/schema/{schemaName}/{collectionName}", params = {"property", "value"})
    public List<DBDocument> findDocumentByIndexedProperty(CollectionId collectionId, @RequestParam String property, @RequestParam String value) {
        return crudService.findByIndexedProperty(new IndexedDocument(collectionId, property, value));
    }
}
