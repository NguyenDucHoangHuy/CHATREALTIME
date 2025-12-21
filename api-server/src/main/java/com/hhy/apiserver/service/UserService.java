package com.hhy.apiserver.service;

import com.hhy.apiserver.dto.request.user.UpdateProfileRequestDTO;
import com.hhy.apiserver.dto.response.UserDTO;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.mapper.UserMapper;
import com.hhy.apiserver.respository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDTO updateProfile(User currentUser, UpdateProfileRequestDTO request) {
        // 1. Cập nhật Username
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            currentUser.setUsername(request.getUsername());
        }

        // 2. Cập nhật Avatar
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            currentUser.setAvatarUrl(request.getAvatarUrl());
        }

        // 3. Cập nhật Ngày sinh
        if (request.getDateOfBirth() != null) {
            currentUser.setDateOfBirth(request.getDateOfBirth());
        }

        // 4. Cập nhật Giới tính (Convert String -> Enum)
        if (request.getGender() != null) {
            try {
                // Chuyển chuỗi "MALE" thành Enum User.Gender.MALE
                User.Gender genderEnum = User.Gender.valueOf(request.getGender().toLowerCase());
                currentUser.setGender(genderEnum);
            } catch (IllegalArgumentException e) {
                // Nếu client gửi sai gender thì có thể bỏ qua hoặc throw lỗi tùy bạn
                // Ở đây ta giữ nguyên gender cũ nếu gửi sai
            }
        }

        // 5. Lưu xuống DB
        User updatedUser = userRepository.save(currentUser);

        // 6. Trả về DTO mới nhất
        return userMapper.toUserDTO(updatedUser);
    }
}