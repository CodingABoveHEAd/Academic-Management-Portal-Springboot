package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.request.StudentCreateRequest;
import com.niloy.student_portal.dto.request.StudentUpdateRequest;
import com.niloy.student_portal.dto.response.ApiResponse;
import com.niloy.student_portal.dto.response.StudentResponse;
import com.niloy.student_portal.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * Create a new student - Only teachers can create students
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @RequestBody StudentCreateRequest request,
            Authentication authentication) {
        StudentResponse response = studentService.createStudent(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student created successfully", response));
    }

    /**
     * Get student by ID - Both teachers and students can view
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all students - Accessible by teachers
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents() {
        List<StudentResponse> response = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get students managed by the authenticated teacher
     */
    @GetMapping("/my-students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getMyStudents(Authentication authentication) {
        List<StudentResponse> response = studentService.getStudentsByTeacher(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get students enrolled in a specific course
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentsByCourse(@PathVariable Long courseId) {
        List<StudentResponse> response = studentService.getStudentsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update student by teacher - full update capability
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudentByTeacher(
            @PathVariable Long id,
            @RequestBody StudentCreateRequest request,
            Authentication authentication) {
        StudentResponse response = studentService.updateStudentByTeacher(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", response));
    }

    /**
     * Delete student - Only teachers can delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @PathVariable Long id,
            Authentication authentication) {
        studentService.deleteStudent(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully", null));
    }
}
