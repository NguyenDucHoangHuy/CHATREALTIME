package com.hhy.apiserver.dto.response;

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
public class UserDTO {
    // Đây là các trường "an toàn" mà client có thể thấy
    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private Date dateOfBirth;
    private User.Gender gender; // Dùng Enum từ Entity
    private String avatarUrl;
    private User.OnlineStatus onlineStatus; // Dùng Enum từ Entity
    private Date lastSeen;
}
