package com.atypon.nosqldbserver.security.jwt;

import lombok.Builder;
import lombok.Data;

import java.util.Date;


@Data
@Builder
public class JWTResponse {
    private String username;
    private String accessToken;
    private String refreshToken;
    private Date accessTokenExpiresAt;
}
