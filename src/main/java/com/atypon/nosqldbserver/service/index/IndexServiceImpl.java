package com.atypon.nosqldbserver.service.index;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.atypon.nosqldbserver.request.CollectionRequest;
import com.atypon.nosqldbserver.request.Pair;
import com.atypon.nosqldbserver.service.documents.DocumentService;
import com.atypon.nosqldbserver.service.file.FileService;
import com.atypon.nosqldbserver.utils.DBFilePath;
import com.atypon.nosqldbserver.utils.DBFileReader;
import com.atypon.nosqldbserver.utils.DBFileWriter;
import com.atypon.nosqldbserver.utils.JSONUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final FileService fileService;
    private final DocumentService documentService;

    @Override
    public LinkedHashMap<String, List<DBDocumentLocation>> load(String path) {
        String indexJSON = DBFileReader.read(path);
        if (!indexJSON.isBlank() && JSONUtils.isValidJSON(indexJSON)) {
            try {
                return new ObjectMapper().readValue(indexJSON, new TypeReference<>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
                throw new JSONParseException(e.getMessage());
            }
        }
        return new LinkedHashMap<>();
    }

    @Override
    public void createIndex(String indexedOn, CollectionRequest request) {
        String indexPath = DBFilePath.buildIndexPath(request, indexedOn);
        String indexesFilePath = DBFilePath.buildIndexesFilePath(request);
        fileService.createFile(indexPath);
        fileService.createFile(indexesFilePath);
        List<Pair<String, String>> indexes = findRegisteredIndexes(request);
        indexes.add(new Pair<>(indexedOn, indexPath));
        DBFileWriter.clear(indexesFilePath);
        DBFileWriter.write(JSONUtils.convertToJSON(indexes), indexesFilePath);
        reIndex(request, indexedOn);
    }

    @Override
    public void reIndex(CollectionRequest request, String indexedOn) {
        String defaultIndexPath = DBFilePath.buildIndexPath(request, "__id__");
        String indexPath = DBFilePath.buildIndexPath(request, indexedOn);
        List<DBDocumentLocation> locations = load(defaultIndexPath).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        List<Map<String, String>> documents = documentService.findAll(request, locations);
        Map<String, List<DBDocumentLocation>> userIndex = new HashMap<>();
        for (int i = 0; i < documents.size(); i++) {
            String value = documents.get(i).get(indexedOn);
            System.out.println(value);
            if (userIndex.containsKey(value)) {
                userIndex.get(value).add(locations.get(i));
            } else {
                List<DBDocumentLocation> list = new ArrayList<>();
                list.add(locations.get(i));
                userIndex.put(value, list);
            }
        }
        DBFileWriter.clear(indexPath);
        DBFileWriter.write(JSONUtils.convertToJSON(userIndex), indexPath);
    }

    @Override
    public void addToIndex(String indexPath, String value, DBDocumentLocation location) {
        Map<String, List<DBDocumentLocation>> index = load(indexPath);
        if (index.containsKey(value)) {
            index.get(value).add(location);
        } else {
            List<DBDocumentLocation> locations = new ArrayList<>();
            locations.add(location);
            index.put(value, locations);
        }
        DBFileWriter.clear(indexPath);
        DBFileWriter.write(JSONUtils.convertToJSON(index), indexPath);
    }

    @Override
    public void removeFromIndex(CollectionRequest collectionRequest, Pair<String, String> keyValue) {
        List<Pair<String, String>> registeredIndexes = findRegisteredIndexes(collectionRequest);
        List<DBDocumentLocation> deletedLocations = removeFromTargetedIndex(collectionRequest, keyValue);

        for (Pair<String, String> p : registeredIndexes) {
            if (p.getKey().equals(keyValue.getKey())) {
                continue;
            }
            LinkedHashMap<String, List<DBDocumentLocation>> index = load(p.getValue());
            List<String> emptyEntries = new ArrayList<>();
            for (Map.Entry<String, List<DBDocumentLocation>> entry : index.entrySet()) {
                entry.getValue().removeAll(deletedLocations);
                if (entry.getValue().size() == 0) {
                    emptyEntries.add(entry.getKey());
                }
            }
            emptyEntries.forEach(index::remove);

            DBFileWriter.clear(p.getValue());
            DBFileWriter.write(JSONUtils.convertToJSON(index), p.getValue());
        }
    }

    @Override
    public List<DBDocumentLocation> getByKey(String indexPath, String key) {
        return load(indexPath).get(key);
    }

    @Override
    public void setByKey(String indexPath, Pair<String, List<DBDocumentLocation>> pair) {
        Map<String, List<DBDocumentLocation>> index = load(indexPath);
        index.put(pair.getKey(), pair.getValue());
        DBFileWriter.clear(indexPath);
        DBFileWriter.write(JSONUtils.convertToJSON(index), indexPath);
    }

    private List<DBDocumentLocation> removeFromTargetedIndex(CollectionRequest collectionRequest, Pair<String, String> keyValue) {
        String indexPath = DBFilePath.buildIndexPath(collectionRequest, keyValue.getKey());
        LinkedHashMap<String, List<DBDocumentLocation>> index = load(indexPath);
        List<DBDocumentLocation> deleted = index.get(keyValue.getValue());
        index.remove(keyValue.getValue());
        DBFileWriter.clear(indexPath);
        DBFileWriter.write(JSONUtils.convertToJSON(index), indexPath);
        return deleted;
    }

    @Override
    public List<Pair<String, String>> findRegisteredIndexes(CollectionRequest request) {
        try {
            String indexesFilePath = DBFilePath.buildIndexesFilePath(request);
            String indexesJSONString = DBFileReader.read(indexesFilePath);
            if (!indexesJSONString.isBlank() && JSONUtils.isValidJSON(indexesJSONString)) {
                return new ObjectMapper().readValue(indexesJSONString, new TypeReference<>() {
                });
            }
            return new ArrayList<>();
        } catch (IOException e) {
            throw new JSONParseException(e.getMessage());
        }
    }

}
