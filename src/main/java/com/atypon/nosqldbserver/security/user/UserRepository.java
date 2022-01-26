package com.atypon.nosqldbserver.security.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<User> findAll();
    Optional<User> findByUsername(String username);
    void register(User user);
    void deleteByUsername(String username);

}
