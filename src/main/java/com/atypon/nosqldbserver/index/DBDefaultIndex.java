package com.atypon.nosqldbserver.index;

import com.atypon.nosqldbserver.access.DBFileAccess;
import com.atypon.nosqldbserver.access.DBFileAccessPool;
import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.exceptions.DBFileNotFoundException;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.atypon.nosqldbserver.access.DBFileReader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import static com.atypon.nosqldbserver.utils.JSONUtils.isValidJSON;

public class DBDefaultIndex extends DBIndex<String, DBDocumentLocation> {

    public DBDefaultIndex(String path) {
        super(path, load(path));
    }

    @Override
    public void put(String key, DBDocumentLocation location) {
        super.indexMap.put(key, location);
    }

    private static LinkedHashMap<String, DBDocumentLocation> load(String path) {
        if (!new File(path).exists()) {
            throw new DBFileNotFoundException("Index file " + path + " not found");
        }
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(path);
        String indexJSON = fileAccess.read();
        if (!indexJSON.isBlank() && isValidJSON(indexJSON)) {
            try {
                return new ObjectMapper().readValue(indexJSON, new TypeReference<>() {
                });
            } catch (IOException e) {
                throw new JSONParseException(e.getMessage());
            }
        }
        return new LinkedHashMap<>();
    }

}
