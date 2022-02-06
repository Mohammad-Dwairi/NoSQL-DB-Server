package com.atypon.nosqldbserver.security.jwt;

import com.atypon.nosqldbserver.security.user.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.Date;


@Data
@Builder
public class JWTResponse {
    private String username;
    private String accessToken;
    private String refreshToken;
    private UserRole role;
    private Date accessTokenExpiresAt;
}
