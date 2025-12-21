package com.hhy.apiserver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // --- Lỗi Chung (General) ---
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(9001, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),

    // --- Lỗi Xác thực (Authentication) 2xxx ---
    USER_EXISTED(2001, "Tên đăng nhập hoặc email đã tồn tại",HttpStatus.BAD_REQUEST),
    LOGIN_FAILED(2002, "Sai tên đăng nhập hoặc mật khẩu",HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(2003, "Bạn chưa xác thực",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2004, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),

    // --- Lỗi Người dùng (User) 4xxx ---
    USER_NOT_FOUND(4004, "Không tìm thấy người dùng",HttpStatus.NOT_FOUND),

    CONVERSATION_NOT_FOUND(6001, "Không tìm thấy người dùng", HttpStatus.BAD_REQUEST);

    // --- Lỗi Bạn bè (Friendship) 5xxx ---
    // (Bạn có thể thêm sau, ví dụ: ALREADY_FRIENDS, REQUEST_PENDING...)

    // --- Lỗi Hội thoại (Conversation) 6xxx ---
    // (Bạn có thể thêm sau)


    private final int code;
    private final String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}