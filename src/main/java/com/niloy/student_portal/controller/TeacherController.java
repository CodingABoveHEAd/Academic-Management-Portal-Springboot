package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.request.TeacherRequest;
import com.niloy.student_portal.dto.response.ApiResponse;
import com.niloy.student_portal.dto.response.TeacherResponse;
import com.niloy.student_portal.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(@RequestBody TeacherRequest request) {
        TeacherResponse response = teacherService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Teacher created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherById(@PathVariable Long id) {
        TeacherResponse response = teacherService.getTeacherById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getAllTeachers() {
        List<TeacherResponse> response = teacherService.getAllTeachers();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getTeachersByDepartment(@PathVariable Long departmentId) {
        List<TeacherResponse> response = teacherService.getTeachersByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(
            @PathVariable Long id,
            @RequestBody TeacherRequest request) {
        TeacherResponse response = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Teacher updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher deleted successfully", null));
    }
}
