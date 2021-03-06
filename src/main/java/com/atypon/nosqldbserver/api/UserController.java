package com.atypon.nosqldbserver.api;

import com.atypon.nosqldbserver.security.user.User;
import com.atypon.nosqldbserver.security.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Profile("master")
@RequiredArgsConstructor
@RequestMapping(path = "/db/users", produces = APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @PostMapping("/register")
    public void registerUser(@RequestBody User user) {
        userService.register(user);
    }

    @GetMapping("/{username}")
    public User getUserByUsername(@PathVariable("username") String username) {
        return userService.findByUsername(username);
    }

    @PutMapping("/{username}/password")
    public void updatePassword(@PathVariable String username, @RequestBody Map<String, String> request) {
        String OLD_PASSWORD = "oldPassword";
        String NEW_PASSWORD = "newPassword";
        if (request.containsKey(NEW_PASSWORD) && request.containsKey(OLD_PASSWORD)) {
            userService.updatePassword(username, request.get(OLD_PASSWORD), request.get(NEW_PASSWORD));
        }
    }

    @DeleteMapping("/{username}")
    public void deleteUser(@PathVariable("username") String username) {
        userService.deleteByUsername(username);
    }

}
