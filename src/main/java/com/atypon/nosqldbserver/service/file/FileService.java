package com.atypon.nosqldbserver.service.file;

public interface FileService {

    boolean createFolders(String path);

    boolean createFile(String path);

    void deleteFolders(String path);

    void deleteFile(String path);

    boolean exists(String path);

    void zip(String targetPath);

    void unzip(String zipFilePath, String destDir);

}
