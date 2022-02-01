package com.atypon.nosqldbserver.aspect;

import com.atypon.nosqldbserver.cache.LRUCache;
import com.atypon.nosqldbserver.core.DBDocument;
import com.atypon.nosqldbserver.helper.CollectionId;
import com.atypon.nosqldbserver.helper.IndexedDocument;
import com.atypon.nosqldbserver.helper.Pair;
import com.atypon.nosqldbserver.service.collection.CollectionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToObjectMap;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class CachingAspect {

    private final CollectionService collectionService;
    private final LRUCache<Pair<CollectionId, String>, List<Object>> cache;

    @SuppressWarnings("unchecked")
    @Around("execution(* com.atypon.nosqldbserver.service.CRUDService.findBy*(..))")
    public Object aroundFindBy(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        IndexedDocument indexedDocument = (IndexedDocument) proceedingJoinPoint.getArgs()[0];
        String queryString = indexedDocument.getIndexedPropertyName() + "=" + indexedDocument.getIndexedPropertyValue();
        Pair<CollectionId, String> cachedEntry = new Pair<>(indexedDocument.getCollectionId(), queryString);
        Optional<List<Object>> cachedData = cache.get(cachedEntry);
        if (cachedData.isPresent()) {
            log.info("RETURNING CACHED DATA");
            return cachedData.get();
        }
        List<Object> retrievedData = (List<Object>) proceedingJoinPoint.proceed();
        if (!retrievedData.isEmpty()) {
            synchronized (cache) {
                cache.put(cachedEntry, retrievedData);
            }
            log.info("RETURNING RETRIEVED DATA");
        }
        return retrievedData;
    }

    @AfterReturning(value = "execution(* com.atypon.nosqldbserver.service.CRUDService.save(..))", returning = "returned")
    public void afterSave(JoinPoint joinPoint, DBDocument returned) {
        log.info("AFTER SAVE");
        System.out.println(returned);
        CollectionId collectionId = (CollectionId) joinPoint.getArgs()[0];
        synchronized (cache) {
            maintainCache(collectionId, returned);
        }
    }


    @Before("execution(* com.atypon.nosqldbserver.service.CRUDService.delete*(..)) || " +
            "execution(* com.atypon.nosqldbserver.service.CRUDService.update*(..)))")
    public void beforeDeleteOrUpdate(JoinPoint joinPoint) {
        log.info("BEFORE DELETE OR UPDATE");
        IndexedDocument indexedDocument = (IndexedDocument) joinPoint.getArgs()[0];
        CollectionId collectionId = indexedDocument.getCollectionId();
        synchronized (cache) {
            Set<Map.Entry<Pair<CollectionId, String>, List<Object>>> cacheEntries = cache.getEntries();
            cacheEntries.removeIf(entry -> entry.getKey().getKey().equals(collectionId));
        }
    }

    private void maintainCache(CollectionId collectionId, Object document) {
        DBDocument dbDocument = (DBDocument) document;
        Map<String, Object> docMap = convertToObjectMap(convertToJSON(dbDocument.getDocument()));
        System.out.println("DOCMAP");
        System.out.println(docMap);
        List<Pair<String, String>> indexes = collectionService.getRegisteredIndexes(collectionId);
        final Pair<CollectionId, String> cacheKey = new Pair<>();
        indexes.forEach(index -> {
            final String indexedPropertyName = index.getKey();
            final String indexedPropertyValue = (String )docMap.get(indexedPropertyName);
            cacheKey.setKey(collectionId);
            cacheKey.setValue(indexedPropertyName + "=" + indexedPropertyValue);
            System.out.println(cacheKey);
            insertIntoCache(cacheKey, dbDocument);
        });
    }

    private void insertIntoCache(Pair<CollectionId, String> key, Object value) {
        Optional<List<Object>> cachedListOptional = cache.get(key);
        if (cachedListOptional.isPresent()) {
            log.info("Adding to existing list");
            List<Object> cachedList = cachedListOptional.get();
            cachedList.add(value);
        } else {
            log.info("Adding new record");
            List<Object> cachedList = new ArrayList<>();
            cachedList.add(value);
            cache.put(key, cachedList);
        }
    }

}
