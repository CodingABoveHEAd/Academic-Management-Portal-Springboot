package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Login endpoint - Uses HTTP Basic Authentication
     * This endpoint validates the credentials and returns user info
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponse.error("Invalid credentials"));
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("role", authentication.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("UNKNOWN"));

        return ResponseEntity.ok(ApiResponse.success("Login successful", userInfo));
    }

    /**
     * Check authentication status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStatus(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponse.error("Not authenticated"));
        }

        Map<String, Object> status = new HashMap<>();
        status.put("authenticated", true);
        status.put("username", authentication.getName());
        status.put("role", authentication.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("UNKNOWN"));

        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
