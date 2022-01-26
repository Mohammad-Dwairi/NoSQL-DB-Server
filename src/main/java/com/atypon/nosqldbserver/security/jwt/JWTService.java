package com.atypon.nosqldbserver.security.jwt;

import com.atypon.nosqldbserver.exceptions.JWTException;
import com.atypon.nosqldbserver.security.user.UserPrincipal;
import com.atypon.nosqldbserver.security.user.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.atypon.nosqldbserver.security.jwt.JWTConstant.*;

@Slf4j
@Service
public class JWTService {

    private final Algorithm algorithm;
    private final UserService userService;

    public JWTService(@Value("${atypon.db.security.hash_key}") String secret, UserService userService) {
        this.algorithm = Algorithm.HMAC256(secret.getBytes());
        this.userService = userService;
    }

    public String getAccessToken(UserPrincipal userPrincipal) {
        List<String> claims = this.getUserAuthorities(userPrincipal);

        return JWT.create()
                .withSubject(userPrincipal.getUsername())
                .withIssuer(DWAIRI)
                .withClaim(AUTHORITIES, claims)
                .withIssuedAt(getTimeAfter(0))
                .withExpiresAt(getTimeAfter(ACCESS_TOKEN_EXPIRATION_TIME))
                .sign(algorithm);
    }

    public String getRefreshToken(UserPrincipal userPrincipal) {
        return JWT.create()
                .withSubject(userPrincipal.getUsername())
                .withIssuer(DWAIRI)
                .withIssuedAt(getTimeAfter(0))
                .withExpiresAt(getTimeAfter(REFRESH_TOKEN_EXPIRATION_TIME))
                .sign(algorithm);
    }

    public JWTResponse buildJWTResponse(String username, String accessToken, String refreshToken) {
        return JWTResponse.builder()
                .username(username)
                .accessToken(accessToken)
                .accessTokenExpiresAt(getTimeAfter(ACCESS_TOKEN_EXPIRATION_TIME))
                .refreshToken(refreshToken)
                .build();
    }

    public String getUsernameFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getSubject();
    }

    public String removeTokenBearerPrefix(String token) {
        if (token!= null && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }
        throw new JWTException("INVALID_TOKEN");
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        DecodedJWT verifiedToken = verifyToken(token);
        String username = verifiedToken.getSubject();
        UserPrincipal userPrincipal = (UserPrincipal) userService.loadUserByUsername(username);
        List<SimpleGrantedAuthority> authorities = this.getAuthoritiesFromToken(token);
        return new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    }

    private DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = this.getJWTVerifier();
        return verifier.verify(token);
    }

    private List<String> getUserAuthorities(UserPrincipal userPrincipal) {
        return userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
    }

    private List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        return verifyToken(token).getClaim(AUTHORITIES).asList(SimpleGrantedAuthority.class);
    }

    private JWTVerifier getJWTVerifier() {
        return JWT.require(algorithm).withIssuer(DWAIRI).build();
    }

    private Date getTimeAfter(long minutes) {
        return new Date(System.currentTimeMillis() + minutes * 60 * 1000);
    }

}
