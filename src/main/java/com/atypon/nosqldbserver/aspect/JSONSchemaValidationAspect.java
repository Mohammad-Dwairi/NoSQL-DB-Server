package com.atypon.nosqldbserver.aspect;

import com.atypon.nosqldbserver.core.DBCollection;
import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.exceptions.CollectionNotFoundException;
import com.atypon.nosqldbserver.exceptions.JSONSchemaValidationException;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.IndexedDocument;
import com.atypon.nosqldbserver.service.collection.CollectionService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;

@Aspect
@Component
@RequiredArgsConstructor
public class JSONSchemaValidationAspect {

    private final CollectionService collectionService;

    @Around("execution(* com.atypon.nosqldbserver.service.CRUDService.save(..))")
    public Object aroundDocumentInsertion(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        CollectionId collectionId = (CollectionId) proceedingJoinPoint.getArgs()[0];
        String document = convertToJSON(proceedingJoinPoint.getArgs()[1]);
        validate(collectionId, document);
        return proceedingJoinPoint.proceed();
    }

    @Around("execution(* com.atypon.nosqldbserver.service.CRUDService.updateByIndexedProperty(..))")
    public Object aroundDocumentUpdateByIndexed(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        IndexedDocument indexedDocument = (IndexedDocument) proceedingJoinPoint.getArgs()[0];
        String document = convertToJSON(proceedingJoinPoint.getArgs()[1]);
        validate(indexedDocument.getCollectionId(), document);
        return proceedingJoinPoint.proceed();
    }

    @Around("execution(* com.atypon.nosqldbserver.service.CRUDService.updateByDefaultId(..))")
    public Object aroundDocumentUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        CollectionId collectionId = (CollectionId) proceedingJoinPoint.getArgs()[0];
        DBDocument document = (DBDocument) proceedingJoinPoint.getArgs()[1];
        validate(collectionId, convertToJSON(document.getDocument()));
        return proceedingJoinPoint.proceed();
    }

    private Object getCollectionSchema(Object arg) {
        CollectionId collectionId = (CollectionId) arg;
        DBCollection collection = collectionService.find(collectionId).orElseThrow(CollectionNotFoundException::new);
        return collection.getSchema();
    }

    private void validate(CollectionId collectionId, String document) {
        SchemaLoader loader = SchemaLoader.builder()
                .schemaJson(getCollectionSchema(collectionId))
                .draftV6Support()
                .build();
        Schema schema = loader.load().build();
        try {
            schema.validate(new JSONObject(document));
        } catch (ValidationException e) {
            String message = e.getCausingExceptions().stream()
                    .map(ValidationException::getMessage)
                    .collect(Collectors.joining(" "));
            throw new JSONSchemaValidationException(e.getMessage() + " " + message);
        }
    }
}
