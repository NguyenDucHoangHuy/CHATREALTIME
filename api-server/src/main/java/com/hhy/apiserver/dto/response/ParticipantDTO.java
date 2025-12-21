package com.hhy.apiserver.dto.response;

import com.hhy.apiserver.entity.Participant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDTO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String role; // "admin" hoặc "member"

    // Constructor từ User entity
    public static ParticipantDTO fromParticipant(Participant p) {
        return new ParticipantDTO(
                p.getUser().getUserId(),
                p.getUser().getUsername(),
                p.getUser().getAvatarUrl(),
                p.getRole().toString()
        );
    }
}