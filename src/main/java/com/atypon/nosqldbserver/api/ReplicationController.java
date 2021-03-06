package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.service.replica.ReplicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Profile("master")
@RestController
@RequestMapping("/db/replica")
@RequiredArgsConstructor
public class ReplicationController {

    private final ReplicaService replicaService;

    @PostMapping
    public void registerReplica(@RequestBody String port, HttpServletRequest request) {
        String replicaAddress = "http://" + request.getRemoteAddr() + ":" + port;
        System.out.println("Address: " + replicaAddress);
        replicaService.register(replicaAddress);
    }

    @GetMapping
    public String getReplicaConnection() {
        return replicaService.getConnection();
    }

}
