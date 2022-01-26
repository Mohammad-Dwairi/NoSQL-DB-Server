package com.atypon.nosqldbserver.service.schema;

import com.atypon.nosqldbserver.access.DBFileAccess;
import com.atypon.nosqldbserver.access.DBFileAccessPool;
import com.atypon.nosqldbserver.core.DBSchema;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.atypon.nosqldbserver.exceptions.SchemaAlreadyExistsException;
import com.atypon.nosqldbserver.exceptions.SchemaNotFoundException;
import com.atypon.nosqldbserver.service.file.FileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.atypon.nosqldbserver.utils.DBFilePath.buildSchemaPath;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;

@Service
public class SchemaServiceImpl implements SchemaService {

    private final FileService fileService;
    private final String schemasFilePath;

    public SchemaServiceImpl(FileService fileService, @Value("${atypon.db.schemas_info_file}") String schemasFilePath) {
        this.fileService = fileService;
        this.schemasFilePath = schemasFilePath;
    }

    @Override
    public List<DBSchema> findAll() {
        if (fileService.exists(schemasFilePath)) {
            DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(schemasFilePath);
            String schemasJSONList = fileAccess.read();
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(schemasJSONList, new TypeReference<>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
                throw new JSONParseException(e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Optional<DBSchema> find(String schema) {
        List<DBSchema> schemas = findAll().stream().filter(s -> s.getName().equals(schema)).collect(Collectors.toList());
        if (schemas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(schemas.get(0));
    }

    @Override
    public void create(String name) {
        if (find(name).isPresent()) {
            throw new SchemaAlreadyExistsException();
        }
        fileService.createFolders(buildSchemaPath(name));
        DBSchema schema = new DBSchema(name);
        List<DBSchema> schemas = findAll();
        schemas.add(schema);
        writeToSchemaFile(schemas);
    }

    @Override
    public void drop(String name) {
        List<DBSchema> schemas = findAll();
        if (schemas.stream().anyMatch(s -> s.getName().equals(name))) {
            fileService.deleteFile(buildSchemaPath(name));
            schemas.removeIf(s -> s.getName().equals(name));
            writeToSchemaFile(schemas);
        } else {
            throw new SchemaNotFoundException();
        }
    }

    @Override
    public void writeToSchemaFile(List<DBSchema> schemas) {
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(schemasFilePath);
        fileAccess.clear();
        fileAccess.write(convertToJSON(schemas));
    }
}
