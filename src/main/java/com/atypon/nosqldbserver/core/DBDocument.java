package com.atypon.nosqldbserver.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DBDocument {
    private String defaultId;
    private Object document;
}
