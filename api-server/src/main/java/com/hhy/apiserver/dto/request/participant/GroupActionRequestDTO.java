package com.hhy.apiserver.dto.request.participant;

import lombok.Data;

@Data
public class GroupActionRequestDTO {
    private Long conversationId;
    private Long targetUserId; // ID của thành viên cần thêm/xóa/thăng chức...
}
