package com.hhy.apiserver.dto.request.conversation;

import lombok.Data;

@Data
public class OpenConversationRequestDTO {
    private Long userId; // ID của người muốn chat cùng (1-1)
}