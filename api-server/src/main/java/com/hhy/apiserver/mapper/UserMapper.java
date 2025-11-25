package com.hhy.apiserver.mapper;

import com.hhy.apiserver.dto.request.auth.RegisterRequestDTO;
import com.hhy.apiserver.dto.response.UserDTO;
import com.hhy.apiserver.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "userId", ignore = true)
    User toUser (RegisterRequestDTO request);


    UserDTO toUserDTO (User user);
}
