package com.atypon.nosqldbserver.exceptions;

public class SchemaAlreadyExistsException extends RuntimeException {
    public SchemaAlreadyExistsException() {
        super("Schema already exists");
    }
}
