package com.hhy.apiserver.dto.request.friendship;

import lombok.Data;

@Data
public class FriendIdRequestDTO {
    private Long targetUserId;
}