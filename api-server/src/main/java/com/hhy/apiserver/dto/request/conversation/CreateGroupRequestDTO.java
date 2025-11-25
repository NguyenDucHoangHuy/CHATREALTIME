package com.hhy.apiserver.dto.request.conversation;

import lombok.Data;
import java.util.List;

@Data
public class CreateGroupRequestDTO {
    private String groupName;
    private List<Long> memberIds; // Danh sách ID thành viên (KHÔNG bao gồm mình)
}
