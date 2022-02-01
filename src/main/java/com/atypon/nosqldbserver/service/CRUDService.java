package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.IndexedDocument;

import java.util.List;

public interface CRUDService {

    List<DBDocument> findAllByDefaultIndex(CollectionId collectionId);

    List<DBDocument> findByIndexedProperty(IndexedDocument indexedDocument);

    List<DBDocument> findByDefaultId(IndexedDocument indexedDocument);

    void save(CollectionId collectionId, Object document);

    void updateByDefaultId(IndexedDocument indexedDocument, Object updatedDocument);

    void updateByIndexedProperty(IndexedDocument indexedDocument, Object updatedDocument);

    void deleteByDefaultId(IndexedDocument indexedDocument);

    void deleteByIndexedProperty(IndexedDocument indexedDocument);

}
