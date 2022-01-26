package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.DocumentId;

import java.util.List;

public interface CRUDService {

    List<DBDocument> findByDefaultId(CollectionId collectionId);

    List<DBDocument> findByIndexedProperty(DocumentId documentId);

    void save(CollectionId collectionId, Object document);

    void updateByDefaultId(CollectionId collectionId, DBDocument updatedDocument);

    void updateByIndexedProperty(DocumentId documentId, Object updatedDocument);

    void deleteByDefaultId(CollectionId collectionId, String defaultId);

    void deleteByIndexedProperty(DocumentId documentId);

}
