package com.atypon.nosqldbserver.service.file;

import com.atypon.nosqldbserver.exceptions.FileCreationException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public void createFolders(String path) {
        File file = new File(path);
        file.mkdirs();
    }

    @Override
    public void createFile(String path) {
        try {
            File file = new File(path);
            file.createNewFile();
        } catch (IOException e) {
            throw new FileCreationException(e.getMessage());
        }
    }

    @Override
    public void deleteFile(String path) {
        File file = new File(path);
        if (!file.delete()) {
            throw new FileCreationException("failed to delete file in path " + path);
        }
    }

    @Override
    public boolean exists(String path) {
        return new File(path).exists();
    }
}
