package com.atypon.nosqldbserver.access;


import com.atypon.nosqldbserver.core.DBDocumentLocation;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DBFileAccess {

    private final String filePath;
    private final ReentrantReadWriteLock readWriteLock;

    DBFileAccess(String filePath) {
        this.filePath = filePath;
        this.readWriteLock = new ReentrantReadWriteLock(true);
    }

    public String read() {
        try {
            readWriteLock.readLock().lock();
            return DBFileReader.read(filePath);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public String read(DBDocumentLocation location) {
        try {
            readWriteLock.readLock().lock();
            return DBFileReader.read(location, filePath);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public List<String> read(List<DBDocumentLocation> locations) {
        try {
            readWriteLock.readLock().lock();
            return DBFileReader.read(locations, filePath);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public List<String> readLines() {
        try {
            readWriteLock.readLock().lock();
            return DBFileReader.readLines(filePath);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public DBDocumentLocation write(String document) {
        try {
            readWriteLock.writeLock().lock();
            return DBFileWriter.write(document, filePath);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public List<DBDocumentLocation> write(List<String> document) {
        try {
            readWriteLock.writeLock().lock();
            return DBFileWriter.write(document, filePath);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void clear() {
        try {
            readWriteLock.writeLock().lock();
            DBFileWriter.clear(filePath);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }


}
