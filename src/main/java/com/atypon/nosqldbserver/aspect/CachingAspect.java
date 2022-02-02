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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToObjectMap;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class CachingAspect {

    private final HttpServletRequest request;

    private final LRUCache<String, List<DBDocument>> cache;

    @SuppressWarnings("unchecked")
    @Around("execution(* com.atypon.nosqldbserver.service.CRUDService.findBy*(..))")
    public Object aroundFindBy(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        final String cachedQuery = request.getRequestURL().append('?').append(request.getQueryString()).toString();
        Optional<List<DBDocument>> cachedData = cache.get(cachedQuery);
        if (cachedData.isPresent()) {
            log.info("RETURNING CACHED DATA");
            return cachedData.get();
        }

        List<DBDocument> retrievedData = (List<DBDocument>) proceedingJoinPoint.proceed();
        if (!retrievedData.isEmpty()) {
            synchronized (cache) {
                cache.put(cachedQuery, retrievedData);
            }
            log.info("RETURNING RETRIEVED DATA");
        }

        return retrievedData;
    }

    @Before("execution(* com.atypon.nosqldbserver.api.DMLWritesController.*(..)) ||" +
            "execution(* com.atypon.nosqldbserver.api.DataSyncController.receiveData(..))")
    public synchronized void beforeDML() {
        this.cache.invalidate();
    }

}
