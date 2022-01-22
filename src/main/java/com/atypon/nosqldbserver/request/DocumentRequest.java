package com.atypon.nosqldbserver.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class DocumentRequest {

    private final CollectionRequest collectionRequest;
    private final String indexedPropertyName;
    private final String indexedPropertyValue;

}
