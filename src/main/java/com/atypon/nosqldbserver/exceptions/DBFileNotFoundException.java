package com.atypon.nosqldbserver.exceptions;

public class DBFileNotFoundException extends RuntimeException {
    public DBFileNotFoundException(String msg) {
        super(msg);
    }
}
