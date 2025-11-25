package com.hhy.apiserver.dto.request.auth;

import lombok.Data;

@Data
public class RefreshTokenRequestDTO {
    private String refreshToken;
}