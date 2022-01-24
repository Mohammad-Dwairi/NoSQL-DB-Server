package com.atypon.nosqldbserver.core;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.beans.ConstructorProperties;

@Getter
@Builder
@EqualsAndHashCode
public class DBDocumentLocation {

    private final long startByte;
    private final long endByte;

    @ConstructorProperties({"startByte", "endByte"})
    public DBDocumentLocation(long startByte, long endByte) {
        this.startByte = startByte;
        this.endByte = endByte;
    }
}
