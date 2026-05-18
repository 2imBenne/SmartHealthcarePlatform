package com.smarthealthcareplatform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException ex) {
        Map<String, String> response = new HashMap<>();
        String rootMsg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        if (rootMsg != null && rootMsg.contains("Duplicate entry")) {
            if (rootMsg.contains("phone_number") || rootMsg.contains("user_profiles")) {
                response.put("message", "Số điện thoại này đã được sử dụng bởi tài khoản khác!");
            } else if (rootMsg.contains("email") || rootMsg.contains("users")) {
                response.put("message", "Email này đã được đăng ký bởi tài khoản khác!");
            } else {
                response.put("message", "Dữ liệu bị trùng lặp trong hệ thống!");
            }
        } else {
            response.put("message", "Lỗi ràng buộc dữ liệu: " + rootMsg);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
