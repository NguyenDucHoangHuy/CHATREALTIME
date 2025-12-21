package com.hhy.apiserver.dto.request.user;

import lombok.Data;

import java.util.Date;

@Data
public class UpdateProfileRequestDTO {
    private String username;
    private String gender;
    private Date dateOfBirth;
    private String avatarUrl;


}
