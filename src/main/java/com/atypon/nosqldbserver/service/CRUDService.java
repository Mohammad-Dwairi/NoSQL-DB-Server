package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.request.CollectionRequest;
import com.atypon.nosqldbserver.request.DocumentRequest;

import java.util.List;
import java.util.Map;

public interface CRUDService {

    List<Map<String, String>> findAll(CollectionRequest colReq);

    List<Map<String, String>> find(DocumentRequest docRequest);

    void save(CollectionRequest colReq, Map<String, String> document);

    void update(DocumentRequest docReq, Map<String, String> updates);

    void delete(DocumentRequest docRequest);

}
