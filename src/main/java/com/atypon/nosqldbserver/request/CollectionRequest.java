package com.atypon.nosqldbserver.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CollectionRequest {

    private final String schemaName;
    private final String collectionName;
}
