package com.atypon.nosqldbserver.exceptions;


public class SchemaNotFoundException extends RuntimeException {
    public SchemaNotFoundException() {
        super("Schema not found");
    }
}
