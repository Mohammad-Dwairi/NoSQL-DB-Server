package com.atypon.nosqldbserver.access;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DBFileWriter {

    static DBDocumentLocation write(String document, String filePath) {
        File file = new File(filePath);
        long fileLength = file.length();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(fileLength);
            raf.write(document.getBytes(StandardCharsets.UTF_8));
            raf.write('\n');
            return DBDocumentLocation.builder().startByte(fileLength).endByte(raf.getFilePointer()).build();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    static List<DBDocumentLocation> write(List<String> documents, String filePath) {
        File file = new File(filePath);
        long fileLength = file.length();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(fileLength);
            List<DBDocumentLocation> locations = new ArrayList<>();
            for (String document : documents) {
                long start = raf.getFilePointer();
                raf.write(document.getBytes(StandardCharsets.UTF_8));
                raf.write('\n');
                locations.add(DBDocumentLocation.builder().startByte(start).endByte(raf.getFilePointer()).build());
            }
            return locations;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    static void clear(String filepath) {
        try {
            PrintWriter printWriter = new PrintWriter(filepath);
            printWriter.print("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
