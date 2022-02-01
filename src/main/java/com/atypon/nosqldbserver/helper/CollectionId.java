package com.atypon.nosqldbserver.helper;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class CollectionId {

    private final String schemaName;
    private final String collectionName;
}
