package com.atypon.nosqldbserver.security.user;

import com.atypon.nosqldbserver.exceptions.DBAuthenticationException;
import com.atypon.nosqldbserver.exceptions.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository, @Lazy BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return new UserPrincipal(user);
        } else {
            throw new UsernameNotFoundException("USER_NOT_FOUND");
        }
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll().stream().filter(user -> !user.getRole().equals(UserRole.ROLE_NODE)).collect(Collectors.toList());
    }

    @Override
    public User findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        throw new UsernameNotFoundException("USER_NOT_FOUND");
    }

    @Override
    public void register(User user) {
        boolean userExists = findAll().stream().anyMatch(u -> u.getUsername().equals(user.getUsername()));
        if (userExists) {
            throw new UserAlreadyExistsException("(" + user.getUsername() + ")" + " user already exists");
        }
        final String password = user.getPassword();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.register(user);
    }

    @Override
    public void updatePassword(String username, String oldPass, String newPass) {
        User user = findByUsername(username);
        if (passwordEncoder.matches(oldPass, user.getPassword())) {
            deleteByUsername(username);
            user.setPassword(newPass);
            register(user);
        } else {
            throw new DBAuthenticationException("Invalid password");
        }
    }

    @Override
    public void deleteByUsername(String username) {
        userRepository.deleteByUsername(username);
    }
}
