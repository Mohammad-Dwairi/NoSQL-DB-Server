package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.core.DBSchema;
import com.atypon.nosqldbserver.exceptions.SchemaNotFoundException;
import com.atypon.nosqldbserver.service.schema.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/db/schemas")
@RequiredArgsConstructor
public class SchemaController {

    private final SchemaService schemaService;
    private final static String SCHEMA_NAME = "schemaName";

    @GetMapping
    public List<DBSchema> findAllSchemas() {
        return schemaService.findAll();
    }

    @GetMapping("/{schemaName}")
    public DBSchema findSchema(@PathVariable String schemaName) {
        return schemaService.find(schemaName).orElseThrow(SchemaNotFoundException::new);
    }

    @GetMapping("{schemaName}/export")
    public void exportSchema(@PathVariable String schemaName, @RequestBody Map<String, String> req) {
        if (req.containsKey("path")) {
            schemaService.exportSchema(schemaName, req.get("path"));
        }
    }

    @PostMapping("/import")
    public void importSchema(@RequestBody DBSchema schema) {
        schemaService.importSchema(schema);
    }

    @PostMapping
    public void createSchema(@RequestBody Map<String, String> request) {
        if (request.containsKey(SCHEMA_NAME)) {
            schemaService.create(request.get(SCHEMA_NAME));
        }
    }

    @DeleteMapping
    public void dropSchema(@RequestBody Map<String, String> request) {
        schemaService.drop(request.get(SCHEMA_NAME));
    }

}
