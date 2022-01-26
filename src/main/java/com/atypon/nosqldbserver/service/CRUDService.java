package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.DocumentId;

import java.util.List;
import java.util.Map;

public interface CRUDService {

    List<Map<String, String>> findByDefaultId(CollectionId colReq);

    List<Map<String, String>> findByIndexedProperty(DocumentId docRequest);

    void save(CollectionId colReq, Map<String, String> document);

    void updateByDefaultId(DocumentId docReq, Map<String, String> updates);

    void updateByIndexedProperty(DocumentId docReq, Map<String, String> updates);

    void deleteByDefaultId(DocumentId docRequest);

    void deleteByIndexedProperty(DocumentId documentId);

}
