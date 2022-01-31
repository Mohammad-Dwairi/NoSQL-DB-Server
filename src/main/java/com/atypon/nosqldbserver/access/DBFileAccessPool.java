package com.atypon.nosqldbserver.access;

import java.util.concurrent.ConcurrentHashMap;

public class DBFileAccessPool {

    private static DBFileAccessPool INSTANCE;
    private final ConcurrentHashMap<String, DBFileAccess> fileAccessMap;

    private DBFileAccessPool() {
        this.fileAccessMap = new ConcurrentHashMap<>();
    }

    public static DBFileAccessPool getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DBFileAccessPool();
        }
        return INSTANCE;
    }

    public DBFileAccess getFileAccess(String filePath) {
        if (fileAccessMap.containsKey(filePath)) {
            return fileAccessMap.get(filePath);
        }
        DBFileAccess fileAccess = new DBFileAccess(filePath);
        fileAccessMap.put(filePath, fileAccess);
        return fileAccess;
    }
}
