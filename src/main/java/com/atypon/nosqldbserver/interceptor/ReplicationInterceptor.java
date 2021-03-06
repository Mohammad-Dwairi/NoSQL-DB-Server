package com.atypon.nosqldbserver.interceptor;

import com.atypon.nosqldbserver.service.replica.ReplicaService;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Profile("master")
@Component
public class ReplicationInterceptor implements HandlerInterceptor {

    private final ReplicaService replicaService;

    public ReplicationInterceptor(@Lazy ReplicaService replicaService) {
        this.replicaService = replicaService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        boolean isResponseOk = response.getStatus() == 200;
        boolean isMutatingRequest = !request.getMethod().equals("GET");
        boolean isReplicaRequest = request.getServletPath().contains("/db/replica");
        boolean shouldNotify =  isResponseOk && isMutatingRequest && !isReplicaRequest;
        if (shouldNotify) {
            replicaService.notifyReplicas();
        }
    }
}
