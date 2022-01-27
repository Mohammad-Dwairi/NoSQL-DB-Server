package com.atypon.nosqldbserver.bootstrap;

import com.atypon.nosqldbserver.security.user.User;
import com.atypon.nosqldbserver.security.user.UserService;
import com.atypon.nosqldbserver.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static com.atypon.nosqldbserver.security.user.UserRole.ROLE_ADMIN;

@Component
@RequiredArgsConstructor
public class DBInitializer implements CommandLineRunner {

    private final FileService fileService;
    private final UserService userService;

    @Override
    public void run(String... args) {
        final String dirsPath = "./data/users";
        final String filePath = dirsPath + "/users.json";
        if (fileService.createFolders(dirsPath) && fileService.createFile(filePath)) {
            User defaultUser = User.builder().username("root").password("root").role(ROLE_ADMIN).build();
            userService.register(defaultUser);
        }
    }
}
