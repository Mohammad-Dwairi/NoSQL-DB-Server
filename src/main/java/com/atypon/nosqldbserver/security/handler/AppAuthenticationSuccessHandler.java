package com.atypon.nosqldbserver.security.handler;

import com.atypon.nosqldbserver.security.jwt.JWTResponse;
import com.atypon.nosqldbserver.security.jwt.JWTService;
import com.atypon.nosqldbserver.security.user.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
public class AppAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        log.info("AUTHENTICATION SUCCESS HANDLER");

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtService.getAccessToken(userPrincipal);
        String refreshToken = jwtService.getRefreshToken(userPrincipal);
        JWTResponse jwtResponse = jwtService.buildJWTResponse(userPrincipal.getUsername(), accessToken, refreshToken);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), jwtResponse);
    }
}
