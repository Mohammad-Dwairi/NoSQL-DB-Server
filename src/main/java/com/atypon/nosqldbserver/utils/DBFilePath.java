package com.atypon.nosqldbserver.utils;

import com.atypon.nosqldbserver.helper.CollectionId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DBFilePath {

    public static String getSchemaDirPath(String schemaName) {
        return "./data/db/" + schemaName;
    }

    public static String getCollectionDirPath(CollectionId collectionId) {
        return getSchemaDirPath(collectionId.getSchemaName()) + "/" + collectionId.getCollectionName();
    }

    public static String getCollectionFilePath(CollectionId collectionId) {
        return getCollectionDirPath(collectionId) + "/" + collectionId.getCollectionName() + ".jsonl";
    }

    public static String getDefaultIndexPath(CollectionId collectionId) {
        return getCollectionDirPath(collectionId) + "/" + collectionId.getCollectionName() + "_index.json";
    }

    public static String getRequestedIndexPath(CollectionId collectionId, String indexedProperty) {
        String col = collectionId.getCollectionName();
        return getCollectionDirPath(collectionId) + "/" + col + "_" + indexedProperty + "_index.json";
    }

    public static String getIndexesFilePath(CollectionId collectionId) {
        return getCollectionDirPath(collectionId) + "/" + collectionId.getCollectionName() + "_indexes.json";
    }

    public static String getUsersFilePath() {
        return "./data/users/users.json";
    }
}
