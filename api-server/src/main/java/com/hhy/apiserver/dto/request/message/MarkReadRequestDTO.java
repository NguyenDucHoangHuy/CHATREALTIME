package com.hhy.apiserver.dto.request.message;

import lombok.Data;

@Data
public class MarkReadRequestDTO {
    private Long conversationId;
    private Long messageId; // ID của tin nhắn vừa đọc (thường là tin nhắn cuối cùng)
}
