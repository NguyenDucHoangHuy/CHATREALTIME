package com.hhy.apiserver.dto.request.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {
    // Có thể đăng nhập email
    // Chúng ta sẽ dùng một trường duy nhất gọi là 'loginIdentifier'
    private String loginIdentifier;
    private String password;
}
