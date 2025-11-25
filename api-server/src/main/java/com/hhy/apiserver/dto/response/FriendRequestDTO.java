package com.hhy.apiserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestDTO {
    private UserDTO requesterInfo; // Ai gửi?
    private Date requestDate;      // Gửi lúc nào? (QUAN TRỌNG)
}
