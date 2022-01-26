package com.atypon.nosqldbserver.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CollectionId {

    private final String schemaName;
    private final String collectionName;
}
