package com.atypon.nosqldbserver.utils;

import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.DocumentId;

public class DBFilePath {

    public static String buildSchemaPath(String schemaName) {
        return "./data/db/" + schemaName;
    }

    public static String buildCollectionPath(CollectionId colReq) {
        return "./data/db/" + colReq.getSchemaName() + "/" + colReq.getCollectionName() + ".jsonl";
    }

    public static String buildDefaultIndexPath(CollectionId request) {
        return "./data/db/" + request.getSchemaName() + "/" + request.getCollectionName() + "_index.json";
    }

    public static String buildRequestedIndexPath(CollectionId collectionId, String indexedProperty) {
        String schema = collectionId.getSchemaName();
        String col = collectionId.getCollectionName();
        return "./data/db/" + schema + "/" + col + "_" + indexedProperty + "_index.json";
    }

    public static String buildIndexesFilePath(CollectionId request) {
        return "./data/db/" + request.getSchemaName() + "/" + request.getCollectionName() + "_indexes.json";
    }

    public static String buildFragmentationTrackerFilePath(CollectionId collectionId) {
        return "./data/db/" + collectionId.getSchemaName() + "/" + collectionId.getCollectionName() + "_frag.json";
    }
}
