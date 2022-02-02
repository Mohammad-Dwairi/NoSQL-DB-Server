package com.atypon.nosqldbserver.service.file;

import com.atypon.nosqldbserver.exceptions.FileCreationException;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.ant.compress.taskdefs.Unzip;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public boolean createFolders(String path) {
        File file = new File(path);
        return file.mkdirs();
    }

    @Override
    public boolean createFile(String path) {
        try {
            File file = new File(path);
            return file.createNewFile();
        } catch (IOException e) {
            throw new FileCreationException(e.getMessage());
        }
    }

    @Override
    public void deleteFolders(String path) {
        FileSystemUtils.deleteRecursively(new File(path));
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

    @Override
    public void zip(String targetPath) {
        try {
            new ZipFile("data.zip").addFolder(new File(targetPath));
        } catch (ZipException e) {
            throw new FileCreationException(e.getMessage());
        }
    }

    @Override
    public void unzip(String zipFilePath, String destDir) {
        Unzip unZipper = new Unzip();
        unZipper.setSrc(new File(zipFilePath));
        unZipper.setDest(new File(destDir));
        unZipper.execute();
    }
}
