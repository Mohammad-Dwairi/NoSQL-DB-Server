package com.atypon.nosqldbserver.service.replica;

import com.atypon.nosqldbserver.exceptions.FileCreationException;
import com.atypon.nosqldbserver.exceptions.NoReplicasFoundException;
import com.atypon.nosqldbserver.security.jwt.JWTService;
import com.atypon.nosqldbserver.security.user.User;
import com.atypon.nosqldbserver.security.user.UserPrincipal;
import com.atypon.nosqldbserver.security.user.UserService;
import com.atypon.nosqldbserver.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplicaServiceImpl implements ReplicaService {

    private final JWTService jwtService;
    private final UserService userService;
    private final FileService fileService;
    private int turn = 0;

    private final List<String> replicaConnections = new ArrayList<>();

    @Override
    public void register(String replicaAddress) {
        replicaConnections.add(replicaAddress);
    }

    @Override
    public void unregister(String internalAddress, String externalAddress) {
        replicaConnections.remove(internalAddress);
    }

    @Override
    public void notifyReplicas() {
        User node = userService.findByUsername("node");
        String token = jwtService.getAccessToken(new UserPrincipal(node));
        WebClient webClient = WebClient.builder().build();
        Resource dataSnapShot = getDataSnapShot();
        for (String replicaAddress : replicaConnections) {
            webClient.post().uri(replicaAddress + "/db/sync")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                    .body(BodyInserters.fromMultipartData("file", dataSnapShot))
                    .retrieve().bodyToMono(String.class).block();
        }
    }

    @Override
    public Resource getDataSnapShot() {
        try {
            Path path = Paths.get("data.zip");
            fileService.zip("./data");
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new FileCreationException(e.getMessage());
        }
    }

    @Override
    public String getConnection() {
        if (replicaConnections.isEmpty()) {
            throw new NoReplicasFoundException("No active read-only connections found");
        }
        String connection = replicaConnections.get(turn++);
        turn %= replicaConnections.size();
        return connection;
    }

    @Override
    public List<String> getReplicasConnections() {
        return replicaConnections;
    }
}
