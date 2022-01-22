package com.atypon.nosqldbserver.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBErrorResponse {
    private int status;
    private String message;
    private long timestamp;
}
