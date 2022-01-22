package com.atypon.nosqldbserver.exceptions;

public class CollectionAlreadyExistsException extends RuntimeException {
    public CollectionAlreadyExistsException() {
        super("Collection already exists");
    }
}
