package com.atypon.nosqldbserver.access;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DBFileReader {

    static String read(String filePath) {
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

    static String read(DBDocumentLocation location, String filePath) {
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

    static List<String> read(List<DBDocumentLocation> locations, String filePath) {
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

    static List<String> readLines(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


}
