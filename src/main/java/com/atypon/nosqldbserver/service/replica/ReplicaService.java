package com.atypon.nosqldbserver.service.replica;

import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ReplicaService {
    void register(String replicaAddress);
    void notifyReplicas();
    Resource getDataSnapShot();
    String getConnection();
}
