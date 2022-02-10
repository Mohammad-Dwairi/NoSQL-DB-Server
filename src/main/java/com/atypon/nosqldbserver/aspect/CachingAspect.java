package com.atypon.nosqldbserver.aspect;

import com.atypon.nosqldbserver.cache.LRUCache;
import com.atypon.nosqldbserver.core.DBDocument;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

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
            return cachedData.get();
        }

        List<DBDocument> retrievedData = (List<DBDocument>) proceedingJoinPoint.proceed();
        if (!retrievedData.isEmpty()) {
            synchronized (cache) {
                cache.put(cachedQuery, retrievedData);
            }
        }

        return retrievedData;
    }

    @Before("execution(* com.atypon.nosqldbserver.api.DMLWritesController.*(..)) ||" +
            "execution(* com.atypon.nosqldbserver.api.DataSyncController.receiveData(..))")
    public synchronized void beforeDML() {
        this.cache.invalidate();
    }

}
