package com.atypon.nosqldbserver.helper;

import lombok.*;

import static lombok.AccessLevel.*;

@Data
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IndexedDocument {

    private CollectionId collectionId;
    private String indexedPropertyName;
    private String indexedPropertyValue;

}
