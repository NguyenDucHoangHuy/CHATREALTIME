package com.hhy.apiserver.dto.request.conversation;

import lombok.Data;

@Data
public class UpdateGroupInfoRequestDTO {
    private String groupName;
    private String groupAvatarUrl;
}
