package com.atypon.nosqldbserver.service.documents;

import com.atypon.nosqldbserver.access.DBFileAccess;
import com.atypon.nosqldbserver.access.DBFileAccessPool;
import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.core.DBDocumentLocation;
import com.atypon.nosqldbserver.helper.CollectionId;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.atypon.nosqldbserver.utils.DBFilePath.getCollectionFilePath;
import static com.atypon.nosqldbserver.utils.JSONUtils.*;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Override
    public List<DBDocument> findAll(CollectionId collectionId) {
        final String collectionPath = getCollectionFilePath(collectionId);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(collectionPath);
        List<String> docsStringList = fileAccess.readLines();
        return convertToDBDocumentList(docsStringList);
    }

    @Override
    public List<DBDocument> findAll(CollectionId collectionId, List<DBDocumentLocation> locations) {
        String collectionPath = getCollectionFilePath(collectionId);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(collectionPath);
        List<String> documentsStringList = fileAccess.read(locations);
        return convertToDBDocumentList(documentsStringList);
    }

    @Override
    public DBDocument find(CollectionId collectionId, DBDocumentLocation location) {
        final String collectionPath = getCollectionFilePath(collectionId);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(collectionPath);
        String documentJSON = fileAccess.read(location);
        return convertToDBDocument(documentJSON);
    }

    @Override
    public DBDocumentLocation save(CollectionId collectionId, DBDocument document) {
        String collectionPath = getCollectionFilePath(collectionId);
        DBFileAccess fileAccess = DBFileAccessPool.getInstance().getFileAccess(collectionPath);
        return fileAccess.write(convertToJSON(document));
    }

}
