package com.atypon.nosqldbserver.exceptions;

public class NoReplicasFoundException extends RuntimeException {
    public NoReplicasFoundException(String message) {
        super(message);
    }
}
