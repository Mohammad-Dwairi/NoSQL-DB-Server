package com.atypon.nosqldbserver.exceptions;

public class DBAuthenticationException extends org.springframework.security.core.AuthenticationException {
    public DBAuthenticationException(String message) {
        super(message);
    }
}
