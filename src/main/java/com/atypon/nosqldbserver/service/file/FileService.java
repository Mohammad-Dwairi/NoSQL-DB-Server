package com.atypon.nosqldbserver.service.file;

public interface FileService {

    void createFolders(String path);

    void createFile(String path);

    void deleteFile(String path);

    boolean exists(String path);
}
