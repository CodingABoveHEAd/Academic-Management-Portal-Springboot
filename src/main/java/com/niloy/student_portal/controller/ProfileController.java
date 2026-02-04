package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.request.StudentUpdateRequest;
import com.niloy.student_portal.dto.response.ApiResponse;
import com.niloy.student_portal.dto.response.StudentResponse;
import com.niloy.student_portal.dto.response.TeacherResponse;
import com.niloy.student_portal.entity.Role;
import com.niloy.student_portal.service.StudentService;
import com.niloy.student_portal.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final StudentService studentService;
    private final TeacherService teacherService;

    /**
     * Get current user's profile
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProfile(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        if (role.equals(Role.ROLE_STUDENT.name())) {
            StudentResponse response = studentService.getStudentByUsername(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(response));
        } else if (role.equals(Role.ROLE_TEACHER.name())) {
            TeacherResponse response = teacherService.getTeacherByUsername(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(response));
        }

        return ResponseEntity.ok(ApiResponse.error("Unknown role"));
    }

    /**
     * Update student profile - Students can only update limited fields
     */
    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @RequestBody StudentUpdateRequest request,
            Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        if (role.equals(Role.ROLE_STUDENT.name())) {
            StudentResponse response = studentService.updateStudentProfile(request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
        }

        return ResponseEntity.ok(ApiResponse.error("Profile update not supported for this role"));
    }
}
