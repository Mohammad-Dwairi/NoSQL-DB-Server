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

    @Value("${atypon.db.hostIP}")
    private String HOST_IP;

    @Value("${atypon.db.mappedPort}")
    private String MAPPED_PORT;

    private final FileService fileService;
    private final JWTService jwtService;

    @Override
    public void run(String... args)  {

        User node = User.builder().username("node").role(UserRole.ROLE_NODE).build();
        String token = jwtService.getAccessToken(new UserPrincipal(node));

        WebClient webClient = WebClient.builder().baseUrl(HOST_IP).build();
        webClient.post().uri("/db/replica").headers(httpHeaders -> {
            httpHeaders.setContentType(valueOf(APPLICATION_JSON_VALUE));
            httpHeaders.setBearerAuth(token);
        }).body(BodyInserters.fromValue(MAPPED_PORT)).retrieve().bodyToMono(String.class).block();


        Path path = Paths.get("./data.zip");
        Flux<DataBuffer> dataBufferFlux = webClient.get()
                .uri("/db/sync")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve().bodyToFlux(DataBuffer.class);
        DataBufferUtils.write(dataBufferFlux, path, StandardOpenOption.CREATE).block();

        fileService.unzip("./data.zip", "./");
    }
}
