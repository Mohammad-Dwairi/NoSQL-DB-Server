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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplicaServiceImpl implements ReplicaService {

    private final JWTService jwtService;
    private final UserService userService;
    private final FileService fileService;
    private int turn = 0;

    private final HttpServletRequest request;

    private final List<String> replicaConnections = new ArrayList<>();

    @Override
    public void register(String replicaAddress) {
        replicaConnections.add(replicaAddress);
    }

    @Override
    public void notifyReplicas() {
        User node = userService.findByUsername("node");
        String token = jwtService.getAccessToken(new UserPrincipal(node));
        WebClient webClient = WebClient.builder().build();
        String requestBody = getRequestBody(request);
        System.out.println(requestBody);
        for (String replicaAddress : replicaConnections) {
            webClient.method(HttpMethod.valueOf(request.getMethod())).uri(replicaAddress + getFullURI())
                    .headers(httpHeaders -> {
                        httpHeaders.setBearerAuth(token);
                        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    })
                    .body(BodyInserters.fromValue(requestBody))
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

    private String getRequestBody(HttpServletRequest request) {
        try {
            return request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException("failed to extract request body");
        }
    }

    public String getFullURI() {
        StringBuilder path = new StringBuilder(request.getServletPath());
        String queryString = request.getQueryString();
        if (queryString == null) {
            return path.toString();
        } else {
            return path.append('?').append(queryString).toString();
        }
    }

}
