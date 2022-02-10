package com.atypon.nosqldbserver.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.beans.ConstructorProperties;

@Getter
@EqualsAndHashCode
public class DBDocumentLocation {

    private final long startByte;
    private final long endByte;

    @ConstructorProperties({"startByte", "endByte"})
    private DBDocumentLocation(long startByte, long endByte) {
        this.startByte = startByte;
        this.endByte = endByte;
    }

    public static DBDocumentLocation create(@NonNull long startByte, @NonNull long endByte) {
        if (startByte >= endByte) {
            throw new RuntimeException("invalid document location");
        }
        return new DBDocumentLocation(startByte, endByte);
    }
}
