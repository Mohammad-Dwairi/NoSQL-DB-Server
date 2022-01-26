package com.atypon.nosqldbserver.index;

import com.atypon.nosqldbserver.access.DBFileAccess;
import com.atypon.nosqldbserver.access.DBFileAccessPool;
import com.atypon.nosqldbserver.exceptions.DBFileNotFoundException;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.atypon.nosqldbserver.utils.JSONUtils.isValidJSON;

public class DBRequestedIndex extends DBIndex<String, List<String>> {

    public DBRequestedIndex(String path) {
        super(path, load(path));
    }

    public void add(String key, String value) {
        if (super.indexMap.containsKey(key)) {
            this.indexMap.get(key).add(value);
        } else {
            List<String> list = new ArrayList<>();
            list.add(value);
            this.indexMap.put(key, list);
        }
    }

    public void clear() {
        super.indexMap.clear();
    }

    public void remove(String key, String value) {
        List<String> postingList = indexMap.get(key);
        if (postingList != null) {
            postingList.remove(value);
        }
    }

    public void remove(String key, List<String> values) {
        List<String> postingList = indexMap.get(key);
        if (postingList != null) {
            postingList.removeIf(values::contains);
            if (postingList.isEmpty()) {
                super.drop(key);
            }
        }

    }

    private static LinkedHashMap<String, List<String>> load(String path) {
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
