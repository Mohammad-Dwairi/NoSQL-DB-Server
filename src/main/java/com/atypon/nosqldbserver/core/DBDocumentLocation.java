package com.atypon.nosqldbserver.core;

import lombok.Builder;
import lombok.Getter;

import java.beans.ConstructorProperties;
import java.util.Objects;

@Getter
@Builder
public class DBDocumentLocation {

    private final long startByte;
    private final long endByte;

    @ConstructorProperties({"startByte", "endByte"})
    public DBDocumentLocation(long startByte, long endByte) {
        this.startByte = startByte;
        this.endByte = endByte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBDocumentLocation location = (DBDocumentLocation) o;
        return startByte == location.startByte && endByte == location.endByte;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startByte, endByte);
    }
}
