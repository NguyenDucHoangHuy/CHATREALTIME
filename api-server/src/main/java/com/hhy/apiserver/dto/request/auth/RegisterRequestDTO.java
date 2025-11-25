package com.hhy.apiserver.dto.request.auth;

import com.hhy.apiserver.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    // Các trường bắt buộc khi đăng ký
    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private Date dateOfBirth;
    private User.Gender gender;

}