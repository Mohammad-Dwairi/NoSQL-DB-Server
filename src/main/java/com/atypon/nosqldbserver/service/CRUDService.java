package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.IndexedDocument;

import java.util.List;

public interface CRUDService {

    List<DBDocument> findByDefaultId(CollectionId collectionId);

    List<DBDocument> findByIndexedProperty(IndexedDocument indexedDocument);

    void save(CollectionId collectionId, Object document);

    void updateByDefaultId(CollectionId collectionId, DBDocument updatedDocument);

    void updateByIndexedProperty(IndexedDocument indexedDocument, Object updatedDocument);

    void deleteByDefaultId(CollectionId collectionId, String defaultId);

    void deleteByIndexedProperty(IndexedDocument indexedDocument);

}
