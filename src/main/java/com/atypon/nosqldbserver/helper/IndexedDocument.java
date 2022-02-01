package com.atypon.nosqldbserver.helper;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class IndexedDocument {

    private CollectionId collectionId;
    private String indexedPropertyName;
    private String indexedPropertyValue;

}
