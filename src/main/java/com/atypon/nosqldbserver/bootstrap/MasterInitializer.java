package com.atypon.nosqldbserver.bootstrap;

import com.atypon.nosqldbserver.security.user.User;
import com.atypon.nosqldbserver.security.user.UserService;
import com.atypon.nosqldbserver.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.atypon.nosqldbserver.security.user.UserRole.ROLE_ADMIN;
import static com.atypon.nosqldbserver.security.user.UserRole.ROLE_NODE;

@Profile("master")
@Component
@RequiredArgsConstructor
public class MasterInitializer implements CommandLineRunner {

    private final FileService fileService;
    private final UserService userService;

    @Override
    public void run(String... args) {
        if (initializeUsersDirectory()) {
            initializeDatabase();
        }
    }

    private void initializeDatabase() {
        User defaultUser = User.builder().username("root").password("root").role(ROLE_ADMIN).build();
        User node = User.builder().username("node").password("node").role(ROLE_NODE).build();
        userService.register(defaultUser);
        userService.register(node);
    }

    private boolean initializeUsersDirectory() {
        final String usersDirPath = "./data/users";
        final String usersFilePath = usersDirPath + "/users.json";
        return fileService.createFolders(usersDirPath) && fileService.createFile(usersFilePath);
    }

}
