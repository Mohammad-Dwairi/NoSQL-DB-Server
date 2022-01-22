package com.atypon.nosqldbserver.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static java.lang.System.currentTimeMillis;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class DBExceptionHandler {

    private final DBErrorResponse errorResponse = new DBErrorResponse();


    @ExceptionHandler(value = {JSONParseException.class, FileCreationException.class})
    public ResponseEntity<Object> internalServerErrorHandler(RuntimeException e) {
        errorResponse.setStatus(INTERNAL_SERVER_ERROR.value());
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(currentTimeMillis());
        return new ResponseEntity<>(errorResponse, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {SchemaNotFoundException.class})
    public ResponseEntity<Object> notFoundHandler(SchemaNotFoundException e) {
        errorResponse.setStatus(NOT_FOUND.value());
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(currentTimeMillis());
        return new ResponseEntity<>(errorResponse, NOT_FOUND);
    }

    @ExceptionHandler(value = {SchemaAlreadyExistsException.class, CollectionAlreadyExistsException.class})
    public ResponseEntity<Object> conflictHandler(RuntimeException e) {
        errorResponse.setStatus(CONFLICT.value());
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(currentTimeMillis());
        return new ResponseEntity<>(errorResponse, CONFLICT);
    }
}
