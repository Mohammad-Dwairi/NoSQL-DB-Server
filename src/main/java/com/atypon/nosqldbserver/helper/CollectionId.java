package com.atypon.nosqldbserver.helper;

import lombok.*;

import static lombok.AccessLevel.*;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class CollectionId {

    private final String schemaName;
    private final String collectionName;

    public static CollectionId create(@NonNull String schemaName, @NonNull String collectionName) {
        return new CollectionId(schemaName, collectionName);
    }
}
