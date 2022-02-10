package com.atypon.nosqldbserver.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class DBDocument {

    private String defaultId;
    private Object document;

    private DBDocument(@NonNull @JsonProperty("defaultId") String defaultId, @NonNull @JsonProperty("document") Object document) {
        this.defaultId = defaultId;
        this.document = document;
    }

    public static DBDocument create(@NonNull String id, @NonNull Object document) {
        return new DBDocument(id, document);
    }
}
