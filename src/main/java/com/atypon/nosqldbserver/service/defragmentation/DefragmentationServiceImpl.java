package com.atypon.nosqldbserver.service.defragmentation;

import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.atypon.nosqldbserver.index.DBDefaultIndex;
import com.atypon.nosqldbserver.request.CollectionId;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.atypon.nosqldbserver.utils.DBFilePath.buildDefaultIndexPath;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;

@Service
@RequiredArgsConstructor
public class DefragmentationServiceImpl implements DefragmentationService {

    private final DocumentService documentService;

    @Override
    public void update(CollectionId collectionId, int indexSize) {
        int currentCollectionSize = documentService.findAll(collectionId).size();
        if (isEligibleForDefragmentation(currentCollectionSize, indexSize)) {
            startDefragmentation(collectionId);
        }
    }

    private void startDefragmentation(CollectionId collectionId) {
        final String defaultIndexPath = buildDefaultIndexPath(collectionId);
        DBDefaultIndex defaultIndex = new DBDefaultIndex(defaultIndexPath);
        List<Map<String, String>> validDocs = documentService.findAll(collectionId, defaultIndex.values());
        List<DBDocumentLocation> newLocations = rewriteDocuments(collectionId, validDocs);
        LinkedHashMap<String, DBDocumentLocation> maintainedIndex = maintainDefaultIndex(validDocs, newLocations);
        String maintainedIndexJSON = convertToJSON(maintainedIndex);
        DBFileWriter.clearAndWrite(maintainedIndexJSON, defaultIndexPath);
    }

    private LinkedHashMap<String, DBDocumentLocation> maintainDefaultIndex(List<Map<String, String>> docs, List<DBDocumentLocation> locations) {
        LinkedHashMap<String, DBDocumentLocation> maintainedIndex = new LinkedHashMap<>();
        for (int i = 0; i < locations.size(); i++) {
            maintainedIndex.put(docs.get(i).get("_$id"), locations.get(i));
        }
        return maintainedIndex;
    }

    private List<DBDocumentLocation> rewriteDocuments(CollectionId collectionId, List<Map<String, String>> docs) {
        String collectionPath = DBFilePath.buildCollectionPath(collectionId);
        List<String> docsJSONList = JSONUtils.convertToJSONList(docs);
        DBFileWriter.clear(collectionPath);
        return DBFileWriter.writeMultiple(docsJSONList, collectionPath);
    }

    private boolean isEligibleForDefragmentation(int collectionCurrentSize, int indexCurrentSize) {
        double defectsRatio = collectionCurrentSize / (double) indexCurrentSize;
        return defectsRatio >= 2;
    }
}
