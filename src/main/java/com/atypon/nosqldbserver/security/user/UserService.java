package com.atypon.nosqldbserver.security.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    List<User> findAll();
    User findByUsername(String username);

    void register(User user);

    void updatePassword(String username, String oldPass, String newPass);
    void deleteByUsername(String username);

}
