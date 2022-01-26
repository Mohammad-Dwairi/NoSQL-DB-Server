package com.atypon.nosqldbserver.service.index;

import com.atypon.nosqldbserver.access.DBFileAccess;
import com.atypon.nosqldbserver.access.DBFileAccessPool;
import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.index.DBDefaultIndex;
import com.atypon.nosqldbserver.index.DBIndex;
import com.atypon.nosqldbserver.index.DBRequestedIndex;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.Pair;
import com.atypon.nosqldbserver.service.collection.CollectionService;
import com.atypon.nosqldbserver.service.defragmentation.DefragmentationService;
import com.atypon.nosqldbserver.utils.JSONUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.atypon.nosqldbserver.utils.DBFilePath.buildDefaultIndexPath;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final CollectionService collectionService;
    private final DefragmentationService defragmentationService;

    @Override
    public void save(CollectionId collectionId, Pair<DBDocument, DBDocumentLocation> docLocationPair) {
        DBDefaultIndex defaultIndex = new DBDefaultIndex(buildDefaultIndexPath(collectionId));
        defaultIndex.put(docLocationPair.getKey().getDefaultId(), docLocationPair.getValue());
        writeIndex(defaultIndex);
        updateRegisteredIndexes(collectionId, docLocationPair.getKey());
        defragmentationService.update(collectionId, defaultIndex.size());
    }

    @Override
    public void update(CollectionId collectionId, Pair<DBDocument, DBDocumentLocation> docLocationPair) {
        DBDefaultIndex defaultIndex = new DBDefaultIndex(buildDefaultIndexPath(collectionId));
        defaultIndex.put(docLocationPair.getKey().getDefaultId(), docLocationPair.getValue());
        writeIndex(defaultIndex);
        reIndexRegisteredIndexes(collectionId);
        defragmentationService.update(collectionId, defaultIndex.size());
    }

    @Override
    public void drop(CollectionId collectionId, String id) {
        DBDefaultIndex defaultIndex = new DBDefaultIndex(buildDefaultIndexPath(collectionId));
        defaultIndex.drop(id);
        writeIndex(defaultIndex);
        reIndexRegisteredIndexes(collectionId);
        defragmentationService.update(collectionId, defaultIndex.size());
    }

    private void reIndexRegisteredIndexes(CollectionId collectionId) {
        collectionService.getRegisteredIndexes(collectionId).forEach(registeredIndex -> {
            collectionService.recoverExistingDocuments(collectionId, registeredIndex.getKey());
        });
    }

    private void updateRegisteredIndexes(CollectionId collectionId, DBDocument updatedDoc) {
        List<Pair<String, String>> registeredIndexes = collectionService.getRegisteredIndexes(collectionId);
        for (Pair<String, String> ri : registeredIndexes) {
            DBRequestedIndex requestedIndex = new DBRequestedIndex(ri.getValue());
            Map<String, Object> docMap = JSONUtils.convertToObjectMap(convertToJSON(updatedDoc.getDocument()));
            final String key = (String) docMap.get(ri.getKey());
            requestedIndex.add(key, updatedDoc.getDefaultId());
            writeIndex(requestedIndex);
        }
    }

    private void writeIndex(DBIndex<?, ?> index) {
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(index.getPath());
        fileAccess.clear();
        fileAccess.write(index.toJSON());
    }


}
