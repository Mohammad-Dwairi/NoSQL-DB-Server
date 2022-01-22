package com.atypon.nosqldbserver.utils;

import com.atypon.nosqldbserver.request.CollectionRequest;

public class DBFilePath {

    public static String buildSchemaPath(String schemaName) {
        return "./data/db/" + schemaName;
    }

    public static String buildCollectionPath(CollectionRequest colReq) {
        return "./data/db/" + colReq.getSchemaName() + "/" + colReq.getCollectionName() + ".jsonl";
    }

    public static String buildDefaultIndexPath(CollectionRequest request) {
        return "./data/db/" + request.getSchemaName() + "/" + request.getCollectionName() + "_index.json";
    }

    public static String buildIndexPath(CollectionRequest request, String indexKeyName) {
        return "./data/db/" + request.getSchemaName() + "/" + request.getCollectionName() + "_" + indexKeyName + "_index.json";
    }

    public static String buildIndexesFilePath(CollectionRequest request) {
        return "./data/db/" + request.getSchemaName() + "/" + request.getCollectionName() + "_indexes.json";
    }
}
