package com.atypon.nosqldbserver.service.replica;

import org.springframework.core.io.Resource;

import java.util.List;

public interface ReplicaService {
    void register(String internalAddress, String externalAddress);
    void unregister(String internalAddress, String externalAddress);
    void notifyReplicas();
    Resource getDataSnapShot();
    String getConnection();
    List<String> getReplicasConnections();
}
