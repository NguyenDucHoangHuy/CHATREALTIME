package com.hhy.apiserver.dto.request.auth;

import lombok.Data;

@Data
public class LogoutRequestDTO {
    private String refreshToken;
}