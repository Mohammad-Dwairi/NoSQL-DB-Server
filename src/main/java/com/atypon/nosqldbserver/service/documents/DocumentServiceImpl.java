package com.atypon.nosqldbserver.service.documents;

import com.atypon.nosqldbserver.access.DBFileAccess;
import com.atypon.nosqldbserver.access.DBFileAccessPool;
import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.DocumentId;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.atypon.nosqldbserver.utils.DBFilePath.buildCollectionPath;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSONList;

@Service
public class DocumentServiceImpl implements DocumentService {


    @Override
    public List<String> findAll(CollectionId collectionId) {
        final String collectionPath = buildCollectionPath(collectionId);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(collectionPath);
        return fileAccess.readLines();
    }

    @Override
    public List<Map<String, String>> findAll(CollectionId collectionId, List<DBDocumentLocation> locations) {
        ObjectMapper mapper = new ObjectMapper();
        String collectionPath = buildCollectionPath(collectionId);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(collectionPath);
        List<String> documentsStringList = fileAccess.read(locations);
        return documentsStringList.stream().map(doc -> {
            try {
                return mapper.readValue(doc, new TypeReference<LinkedHashMap<String, String>>() {
                });
            } catch (IOException e) {
                throw new JSONParseException(e.getMessage());
            }
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> find(CollectionId collectionId, DBDocumentLocation location) {
        final String collectionPath = buildCollectionPath(collectionId);
        final ObjectMapper mapper = new ObjectMapper();
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(collectionPath);
        String documentJSON = fileAccess.read(location);
        try {
            return mapper.readValue(documentJSON, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new JSONParseException(e.getMessage());
        }
    }

    @Override
    public DBDocumentLocation save(CollectionId collectionId, Map<String, String> document) {
        String collectionPath = buildCollectionPath(collectionId);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(collectionPath);
        return fileAccess.write(convertToJSON(document));
    }

    @Override
    public List<DBDocumentLocation> saveAll(CollectionId collectionId, List<Map<String, String>> document) {
        String collectionPath = buildCollectionPath(collectionId);
        List<String> docsJSON = convertToJSONList(document);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(collectionPath);
        return fileAccess.write(docsJSON);
    }

    @Override
    public void delete(DocumentId documentId) {

    }


}
