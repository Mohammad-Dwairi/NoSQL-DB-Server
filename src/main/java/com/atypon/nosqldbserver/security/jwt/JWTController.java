package com.atypon.nosqldbserver.security.jwt;

import com.atypon.nosqldbserver.security.user.User;
import com.atypon.nosqldbserver.security.user.UserPrincipal;
import com.atypon.nosqldbserver.security.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RestController
@RequestMapping(path = "/token")
@RequiredArgsConstructor
public class JWTController {

    private final UserService userService;
    private final JWTService jwtService;

    @PostMapping("/refresh")
    public JWTResponse refreshToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        String refreshToken = jwtService.removeTokenBearerPrefix(authorizationHeader);
        String username = jwtService.getUsernameFromToken(refreshToken);
        User user = userService.findByUsername(username);
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String accessToken = jwtService.getAccessToken(userPrincipal);
        return jwtService.buildJWTResponse(username, accessToken, refreshToken);
    }
}
