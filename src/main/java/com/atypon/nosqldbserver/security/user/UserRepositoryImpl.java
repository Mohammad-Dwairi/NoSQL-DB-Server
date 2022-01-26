package com.atypon.nosqldbserver.security.user;

import com.atypon.nosqldbserver.access.DBFileAccess;
import com.atypon.nosqldbserver.access.DBFileAccessPool;
import com.atypon.nosqldbserver.exceptions.JSONParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.atypon.nosqldbserver.utils.DBFilePath.buildUsersFilePath;
import static com.atypon.nosqldbserver.utils.JSONUtils.convertToJSON;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final DBFileAccess fileAccess;

    public UserRepositoryImpl() {
        this.fileAccess = DBFileAccessPool.getInstance().getFileAccess(buildUsersFilePath());
    }

    @Override
    public List<User> findAll() {
        String usersJSON = fileAccess.read();
        if (!usersJSON.isBlank()) {
            try {
                return new ObjectMapper().readValue(usersJSON, new TypeReference<>() {
                });
            } catch (IOException e) {
                throw new JSONParseException(e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findAll().stream().filter(user -> user.getUsername().equals(username)).findFirst();
    }

    @Override
    public void register(User user) {
        List<User> users = findAll();
        users.add(user);
        fileAccess.clear();
        fileAccess.write(convertToJSON(users));
    }

    @Override
    public void deleteByUsername(String username) {
        List<User> users = findAll();
        boolean removed = users.removeIf(user -> user.getUsername().equals(username));
        if (removed) {
            fileAccess.clear();
            fileAccess.write(convertToJSON(users));
        }
    }
}
