package com.atypon.nosqldbserver.utils;

import com.atypon.nosqldbserver.core.DBDocumentLocation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DBFileReader {

    public static String read(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String readAt(String filePath, DBDocumentLocation location) {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            raf.seek(location.getStartByte());
            byte[] buffer = new byte[(int) (location.getEndByte() - location.getStartByte())];
            raf.readFully(buffer);
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static List<String> readMultiple(String filePath, List<DBDocumentLocation> locations) {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            List<String> matches = new ArrayList<>();
            for (DBDocumentLocation location : locations) {
                raf.seek(location.getStartByte());
                byte[] buffer = new byte[(int) (location.getEndByte() - location.getStartByte())];
                raf.readFully(buffer);
                matches.add(new String(buffer));
            }
            return matches;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
