package com.atypon.nosqldbserver.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class IndexedDocument {

    private final CollectionId collectionId;
    private final String indexedPropertyName;
    private final String indexedPropertyValue;

}
