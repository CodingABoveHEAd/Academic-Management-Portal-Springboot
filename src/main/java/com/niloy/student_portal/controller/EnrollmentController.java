package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.request.EnrollmentRequest;
import com.niloy.student_portal.dto.response.ApiResponse;
import com.niloy.student_portal.dto.response.StudentResponse;
import com.niloy.student_portal.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

    private final StudentService studentService;

    /**
     * Enroll in a course - Students only, can only enroll in one course at a time
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> enrollInCourse(
            @RequestBody EnrollmentRequest request,
            Authentication authentication) {
        StudentResponse response = studentService.enrollInCourse(request.getCourseId(), authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Successfully enrolled in course", response));
    }

    /**
     * Drop current course enrollment
     */
    @DeleteMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> dropCourse(Authentication authentication) {
        StudentResponse response = studentService.dropCourse(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Successfully dropped course", response));
    }

    /**
     * View current enrollment status
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> getEnrollmentStatus(Authentication authentication) {
        StudentResponse response = studentService.getStudentByUsername(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
