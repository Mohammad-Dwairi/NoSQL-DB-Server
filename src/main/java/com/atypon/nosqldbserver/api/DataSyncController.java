package com.atypon.nosqldbserver.api;


import com.atypon.nosqldbserver.service.file.FileService;
import com.atypon.nosqldbserver.service.replica.ReplicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/db/sync")
@RequiredArgsConstructor
public class DataSyncController {

    private final ReplicaService replicaService;
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<?> getData() {
        Resource resource = replicaService.getDataSnapShot();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping
    public void receiveData(@RequestPart("file") MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        Path path = Paths.get("./data.zip");
        Files.write(path, bytes);
        fileService.unzip("./data.zip", "./");
    }
}
