package com.atypon.nosqldbserver.security.filter;

import com.atypon.nosqldbserver.exceptions.DBErrorResponse;
import com.atypon.nosqldbserver.interceptor.BufferedServletRequestWrapper;
import com.atypon.nosqldbserver.security.jwt.JWTService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.atypon.nosqldbserver.security.jwt.JWTConstant.TOKEN_PREFIX;
import static java.lang.System.currentTimeMillis;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
public class AppAuthorizationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        BufferedServletRequestWrapper wrappedRequest = new BufferedServletRequestWrapper(request);
        try {
            String authorizationHeader = wrappedRequest.getHeader(AUTHORIZATION);
            if (isCorrectAuthorizationHeader(authorizationHeader)) {
                authorizeUser(authorizationHeader);
            }
            filterChain.doFilter(wrappedRequest, response);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            SecurityContextHolder.clearContext();
            response.setStatus(FORBIDDEN.value());
            response.setContentType(APPLICATION_JSON_VALUE);
            DBErrorResponse errorResponse = new DBErrorResponse(FORBIDDEN.value(), e.getMessage(), currentTimeMillis());
            new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
        }
    }

    private boolean isCorrectAuthorizationHeader(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX);
    }

    private void authorizeUser(String authorizationHeader) {
        String token = jwtService.removeTokenBearerPrefix(authorizationHeader);
        UsernamePasswordAuthenticationToken authenticationToken = jwtService.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
