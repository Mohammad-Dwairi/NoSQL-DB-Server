package com.atypon.nosqldbserver.security.filter;

import com.atypon.nosqldbserver.exceptions.DBErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.System.currentTimeMillis;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class AppAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {

        DBErrorResponse errorResponse = new DBErrorResponse();
        errorResponse.setMessage("Authentication is required to access this resource");
        errorResponse.setStatus(FORBIDDEN.value());
        errorResponse.setTimestamp(currentTimeMillis());

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(FORBIDDEN.value());

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
