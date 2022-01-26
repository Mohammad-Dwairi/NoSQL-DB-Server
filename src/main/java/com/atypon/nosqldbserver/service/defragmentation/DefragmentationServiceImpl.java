package com.atypon.nosqldbserver.service.defragmentation;

import com.atypon.nosqldbserver.access.DBFileAccess;
import com.atypon.nosqldbserver.access.DBFileAccessPool;
import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.index.DBDefaultIndex;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.service.documents.DocumentService;
import com.atypon.nosqldbserver.utils.DBFilePath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static com.atypon.nosqldbserver.utils.DBFilePath.buildDefaultIndexPath;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSONList;

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
        List<DBDocument> validDocs = documentService.findAll(collectionId, defaultIndex.values());
        List<DBDocumentLocation> newLocations = rewriteDocuments(collectionId, validDocs);
        LinkedHashMap<String, DBDocumentLocation> maintainedIndex = maintainDefaultIndex(validDocs, newLocations);
        String maintainedIndexJSON = convertToJSON(maintainedIndex);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(defaultIndexPath);
        fileAccess.clear();
        fileAccess.write(maintainedIndexJSON);
    }

    private LinkedHashMap<String, DBDocumentLocation> maintainDefaultIndex(List<DBDocument> docs, List<DBDocumentLocation> locations) {
        LinkedHashMap<String, DBDocumentLocation> maintainedIndex = new LinkedHashMap<>();
        for (int i = 0; i < locations.size(); i++) {
            maintainedIndex.put(docs.get(i).getDefaultId(), locations.get(i));
        }
        return maintainedIndex;
    }

    private List<DBDocumentLocation> rewriteDocuments(CollectionId collectionId, List<DBDocument> docs) {
        String collectionPath = DBFilePath.buildCollectionPath(collectionId);
        List<String> docsJSONList = convertToJSONList(Arrays.asList(docs.toArray()));
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(collectionPath);
        fileAccess.clear();
        return fileAccess.write(docsJSONList);
    }

    private boolean isEligibleForDefragmentation(int collectionCurrentSize, int indexCurrentSize) {
        double defectsRatio = collectionCurrentSize / (double) indexCurrentSize;
        return defectsRatio >= 2;
    }
}
