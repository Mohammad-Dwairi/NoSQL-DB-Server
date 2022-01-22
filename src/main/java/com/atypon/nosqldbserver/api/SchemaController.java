package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.core.DBSchema;
import com.atypon.nosqldbserver.service.schema.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/db")
@RequiredArgsConstructor
public class SchemaController {

    private final SchemaService schemaService;
    private final static String SCHEMA_NAME = "schemaName";

    @GetMapping
    public List<DBSchema> findAllSchemas() {
        return schemaService.findAll();
    }

    @PostMapping
    public void createSchema(@RequestBody Map<String, String> request) {
        schemaService.create(request.get(SCHEMA_NAME));
    }

    @DeleteMapping
    public void dropSchema(@RequestBody Map<String, String> request) {
        schemaService.drop(request.get(SCHEMA_NAME));
    }

}
