package com.atypon.nosqldbserver.service.defragmentation;

import com.atypon.nosqldbserver.request.CollectionId;

public interface DefragmentationService {
    void update(CollectionId collectionId, int indexSize);
}