package com.atypon.nosqldbserver.service.documents;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.atypon.nosqldbserver.request.CollectionId;
import com.atypon.nosqldbserver.request.DocumentId;
import com.atypon.nosqldbserver.utils.DBFileReader;
import com.atypon.nosqldbserver.utils.DBFileWriter;
import com.atypon.nosqldbserver.utils.JSONUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.atypon.nosqldbserver.utils.DBFilePath.buildCollectionPath;

@Service
public class DocumentServiceImpl implements DocumentService {


    @Override
    public List<String> findAll(CollectionId collectionId) {
        final String collectionPath = buildCollectionPath(collectionId);
        return DBFileReader.readLines(collectionPath);
    }

    @Override
    public List<Map<String, String>> findAll(CollectionId collectionId, List<DBDocumentLocation> locations) {
        ObjectMapper mapper = new ObjectMapper();
        String collectionPath = buildCollectionPath(collectionId);
        List<String> documentsStringList = DBFileReader.readMultiple(collectionPath, locations);
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
        String documentJSON = DBFileReader.readAt(collectionPath, location);
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
        return DBFileWriter.write(JSONUtils.convertToJSON(document), collectionPath);
    }

    @Override
    public List<DBDocumentLocation> saveAll(CollectionId collectionId, List<Map<String, String>> document) {
        String collectionPath = buildCollectionPath(collectionId);
        List<String> docsJSON = JSONUtils.convertToJSONList(document);
        return DBFileWriter.writeMultiple(docsJSON, collectionPath);
    }

    @Override
    public void delete(DocumentId documentId) {

    }


}
