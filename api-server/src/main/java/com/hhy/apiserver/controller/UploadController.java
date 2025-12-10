package com.hhy.apiserver.controller;

import com.hhy.apiserver.dto.response.ApiResponse;
import com.hhy.apiserver.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = cloudinaryService.uploadFile(file,"chat-app/message");
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .code(1000).message("Upload thành công").result(url).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .code(500).message("Lỗi upload: " + e.getMessage()).build());
        }
    }
}