package com.atypon.nosqldbserver.service;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.request.CollectionRequest;
import com.atypon.nosqldbserver.request.DocumentRequest;
import com.atypon.nosqldbserver.request.Pair;
import com.atypon.nosqldbserver.service.documents.DocumentService;
import com.atypon.nosqldbserver.service.index.IndexService;
import com.atypon.nosqldbserver.utils.DBFilePath;
import com.atypon.nosqldbserver.utils.DBFileWriter;
import com.atypon.nosqldbserver.utils.JSONUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CRUDServiceImpl implements CRUDService {

    private final DocumentService documentService;
    private final IndexService indexService;

    @Override
    public List<Map<String, String>> findAll(CollectionRequest colReq) {
        String indexPath = DBFilePath.buildIndexPath(colReq, "__id__");
        Map<String, List<DBDocumentLocation>> index = indexService.load(indexPath);
        List<DBDocumentLocation> locations = index.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        return documentService.findAll(colReq, locations);
    }

    @Override
    public List<Map<String, String>> find(DocumentRequest docRequest) {
        System.out.println(docRequest);
        String indexPath = DBFilePath.buildIndexPath(docRequest.getCollectionRequest(), docRequest.getIndexedPropertyName());
        List<DBDocumentLocation> locations = indexService.load(indexPath).get(docRequest.getIndexedPropertyValue());
        if (locations == null) {
            locations = new ArrayList<>();
        }
        return documentService.findAll(docRequest.getCollectionRequest(), locations);
    }

    @Override
    public void save(CollectionRequest colReq, Map<String, String> document) {
        document.put("__id__", generateDefaultId());
        DBDocumentLocation location = documentService.save(colReq, document);
        List<Pair<String, String>> registeredIndexesPaths = indexService.findRegisteredIndexes(colReq);
        for (Pair<String, String> indexKeyPathPair : registeredIndexesPaths) {
            String indexedOn = indexKeyPathPair.getKey();
            String indexFilePath = indexKeyPathPair.getValue();
            String value = document.get(indexedOn);
            indexService.addToIndex(indexFilePath, value, location);
        }
    }

    @Override
    public void update(DocumentRequest docReq, Map<String, String> updates) {
        List<Map<String, String>> docs = find(docReq);
        docs.forEach(doc -> doc.putAll(updates));
        List<DBDocumentLocation> locations = documentService.saveAll(docReq.getCollectionRequest(), docs);
        for (Pair<String, String> p : indexService.findRegisteredIndexes(docReq.getCollectionRequest())) {
            String indexPath = p.getValue();
            String indexedOn = p.getKey();
            var index = indexService.load(indexPath);

            for (int i = 0; i < docs.size(); i++) {
                if (index.containsKey(docs.get(i).get(indexedOn))) {
                    index.get(docs.get(i).get(indexedOn)).set(i, locations.get(i));
                }
                else {
                    index.put(docs.get(i).get(indexedOn), List.of(locations.get(i)));
                }
            }
            DBFileWriter.clear(indexPath);
            DBFileWriter.write(JSONUtils.convertToJSON(index), indexPath);
        }
    }

    @Override
    public void delete(DocumentRequest docRequest) {
        indexService.removeFromIndex(docRequest.getCollectionRequest(), new Pair<>(docRequest.getIndexedPropertyName(), docRequest.getIndexedPropertyValue()));
    }

    private String generateDefaultId() {
        return String.valueOf(Timestamp.from(Instant.now()).getTime());
    }

}
