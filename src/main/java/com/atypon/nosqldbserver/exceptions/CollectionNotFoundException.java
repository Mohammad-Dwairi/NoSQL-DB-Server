package com.atypon.nosqldbserver.exceptions;

public class CollectionNotFoundException extends RuntimeException {

    public CollectionNotFoundException() {
        super("Collection not found");
    }
}
