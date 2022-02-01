package com.atypon.nosqldbserver.bootstrap;

import com.atypon.nosqldbserver.security.jwt.JWTService;
import com.atypon.nosqldbserver.security.user.User;
import com.atypon.nosqldbserver.security.user.UserPrincipal;
import com.atypon.nosqldbserver.security.user.UserRole;
import com.atypon.nosqldbserver.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.valueOf;

@Profile("replica")
@Component
@RequiredArgsConstructor
public class ReplicaInitializer implements CommandLineRunner {

    @Value("${atypon.db.host.address}")
    private String HOST_ADDRESS;

    @Value("${server.port}")
    private String PORT;

    private final FileService fileService;
    private final JWTService jwtService;

    @Override
    public void run(String... args) {
        final String nodeToken = generateNodeToken();
        final WebClient webClient = buildWebClient(nodeToken);
        sendRegistrationRequestToMaster(webClient);
        syncReplicaWithMaster(webClient);
    }

    private String generateNodeToken() {
        User node = User.builder().username("node").role(UserRole.ROLE_NODE).build();
        return jwtService.getAccessToken(new UserPrincipal(node));
    }

    private WebClient buildWebClient(String token) {
        return WebClient.builder()
                .baseUrl(HOST_ADDRESS)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setContentType(valueOf(APPLICATION_JSON_VALUE));
                    httpHeaders.setBearerAuth(token);
                }).build();
    }

    private void sendRegistrationRequestToMaster(WebClient webClient) {
        webClient.post().uri("/db/replica")
                .body(BodyInserters.fromValue(PORT))
                .retrieve().bodyToMono(String.class).block();
    }

    private void syncReplicaWithMaster(WebClient webClient) {
        Path path = Paths.get("./data.zip");
        Flux<DataBuffer> dataBufferFlux = webClient.get().uri("/db/sync")
                .retrieve().bodyToFlux(DataBuffer.class);
        DataBufferUtils.write(dataBufferFlux, path, StandardOpenOption.CREATE).block();
        fileService.unzip(path.toString(), "./");
    }
}
