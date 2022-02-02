package com.atypon.nosqldbserver.utils;

import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JSONUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean isValidJSON(String jsonString) {
        try {
            mapper.readTree(jsonString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static List<String> convertToJSONList(List<Object> list) {
        return list.stream().map(item -> {
            try {
                return mapper.writeValueAsString(item);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }).collect(Collectors.toList());
    }

    public static String convertToJSON(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Map<String, Object> convertToObjectMap(String docJSON) {
        try {
            return mapper.readValue(docJSON, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new JSONParseException(e.getMessage());
        }
    }

    public static DBDocument convertToDBDocument(String docJSON) {
        try {
            return mapper.readValue(docJSON, DBDocument.class);
        } catch (IOException e) {
            throw new JSONParseException(e.getMessage());
        }
    }

    public static List<DBDocument> convertToDBDocumentList(List<String> docJSONList) {
        return docJSONList.stream().map(JSONUtils::convertToDBDocument).collect(Collectors.toList());
    }
}
