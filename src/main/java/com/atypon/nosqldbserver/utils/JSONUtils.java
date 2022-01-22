package com.atypon.nosqldbserver.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JSONUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean isValidJSON(String jsonString) { // NOT WORKING :(
        try {
            mapper.readTree(jsonString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static List<String> convertToJSONList(List<Map<String, String>> list) {
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
}
