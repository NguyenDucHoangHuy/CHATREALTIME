package com.hhy.apiserver.controller;

import com.hhy.apiserver.dto.request.participant.GroupActionRequestDTO;
import com.hhy.apiserver.dto.response.ApiResponse;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    // 1. Thêm thành viên
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addMember(
            @AuthenticationPrincipal User currentUser,
            @RequestBody GroupActionRequestDTO request) {

        participantService.addMember(currentUser, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã thêm thành viên").build());
    }

    // 2. Xóa thành viên (Kick)
    @PostMapping("/remove")
    public ResponseEntity<ApiResponse<String>> removeMember(
            @AuthenticationPrincipal User currentUser,
            @RequestBody GroupActionRequestDTO request) {

        participantService.removeMember(currentUser, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã xóa thành viên khỏi nhóm").build());
    }

    // 3. Rời nhóm
    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<String>> leaveGroup(
            @AuthenticationPrincipal User currentUser,
            @RequestBody GroupActionRequestDTO request) {

        participantService.leaveGroup(currentUser, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã rời nhóm").build());
    }

    // 4. Thăng cấp
    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<String>> promoteAdmin(
            @AuthenticationPrincipal User currentUser,
            @RequestBody GroupActionRequestDTO request) {

        participantService.promoteAdmin(currentUser, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã thăng cấp thành viên lên Admin").build());
    }

    // 5. Hạ cấp
    @PostMapping("/demote")
    public ResponseEntity<ApiResponse<String>> demoteAdmin(
            @AuthenticationPrincipal User currentUser,
            @RequestBody GroupActionRequestDTO request) {

        participantService.demoteAdmin(currentUser, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã hạ cấp Admin xuống thành viên").build());
    }
}