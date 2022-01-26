package com.atypon.nosqldbserver.aspect;

import com.atypon.nosqldbserver.cache.LRUCache;
import com.atypon.nosqldbserver.request.DocumentId;
import com.atypon.nosqldbserver.request.Pair;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class CachingAspect {

    private final LRUCache<Pair<String, String>, Object> cache;

    @Around("execution(* com.atypon.nosqldbserver.service.CRUDService.findByIndexedProperty(..))")
    public Object aroundFind(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        DocumentId documentId = (DocumentId) proceedingJoinPoint.getArgs()[0];
        Pair<String, String> query = new Pair<>(documentId.getIndexedPropertyName(), documentId.getIndexedPropertyValue());
        Optional<Object> cachedData = cache.get(query);
        if (cachedData.isPresent()) {
            log.info("RETURNING CACHED DATA");
            return cachedData.get();
        }
        Object retrievedData = proceedingJoinPoint.proceed();
        cache.put(query, retrievedData);
        log.info("RETURNING RETRIEVED DATA");
        return retrievedData;
    }


    @Before("execution(* com.atypon.nosqldbserver.service.CRUDService.*IndexedProperty(..)))")
    public void beforeUpdateOrUpdate(JoinPoint joinPoint) {
        log.info("BEFORE UPDATE OR DELETE");
        DocumentId documentId = (DocumentId) joinPoint.getArgs()[0];
        Pair<String, String> query = new Pair<>(documentId.getIndexedPropertyName(), documentId.getIndexedPropertyValue());
        Optional<Object> cachedData = cache.get(query);
        if (cachedData.isPresent()) {
           cache.drop(query);
        }
    }

}
