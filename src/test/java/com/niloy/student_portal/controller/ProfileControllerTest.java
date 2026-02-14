package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.request.StudentUpdateRequest;
import com.niloy.student_portal.dto.response.ApiResponse;
import com.niloy.student_portal.dto.response.StudentResponse;
import com.niloy.student_portal.dto.response.TeacherResponse;
import com.niloy.student_portal.entity.Role;
import com.niloy.student_portal.service.StudentService;
import com.niloy.student_portal.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileController Tests")
class ProfileControllerTest {

    @Mock
    private StudentService studentService;

    @Mock
    private TeacherService teacherService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProfileController profileController;

    private StudentResponse studentResponse;
    private TeacherResponse teacherResponse;
    private StudentUpdateRequest studentUpdateRequest;

    @BeforeEach
    void setUp() {
        studentResponse = StudentResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .studentId("STU001")
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .address("123 Main St")
                .phoneNumber("1234567890")
                .email("john.doe@email.com")
                .teacherName("Jane Smith")
                .teacherId(1L)
                .build();

        teacherResponse = TeacherResponse.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .employeeId("EMP001")
                .specialization("Computer Science")
                .departmentName("Computer Science")
                .departmentId(1L)
                .email("jane.smith@email.com")
                .studentCount(5)
                .build();

        studentUpdateRequest = new StudentUpdateRequest();
        studentUpdateRequest.setFirstName("John Updated");
        studentUpdateRequest.setLastName("Doe");
        studentUpdateRequest.setAddress("456 New St");
        studentUpdateRequest.setPhoneNumber("9876543210");
        studentUpdateRequest.setEmail("john.updated@email.com");
    }

    @Nested
    @DisplayName("Get Profile Tests")
    class GetProfileTests {

        @Test
        @DisplayName("Should return student profile for student role")
        void getProfile_AsStudent_ShouldReturnStudentProfile() {
            // Arrange
            when(authentication.getName()).thenReturn("john.doe");
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_STUDENT.name()));
            when(authentication.getAuthorities()).thenReturn(authorities);
            when(studentService.getStudentByUsername("john.doe")).thenReturn(studentResponse);

            // Act
            ResponseEntity<ApiResponse<?>> response = profileController.getProfile(authentication);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            StudentResponse data = (StudentResponse) response.getBody().getData();
            assertEquals("John", data.getFirstName());
            assertEquals("STU001", data.getStudentId());
            verify(studentService, times(1)).getStudentByUsername("john.doe");
            verify(teacherService, never()).getTeacherByUsername(anyString());
        }

        @Test
        @DisplayName("Should return teacher profile for teacher role")
        void getProfile_AsTeacher_ShouldReturnTeacherProfile() {
            // Arrange
            when(authentication.getName()).thenReturn("jane.smith");
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_TEACHER.name()));
            when(authentication.getAuthorities()).thenReturn(authorities);
            when(teacherService.getTeacherByUsername("jane.smith")).thenReturn(teacherResponse);

            // Act
            ResponseEntity<ApiResponse<?>> response = profileController.getProfile(authentication);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            TeacherResponse data = (TeacherResponse) response.getBody().getData();
            assertEquals("Jane", data.getFirstName());
            assertEquals("EMP001", data.getEmployeeId());
            verify(teacherService, times(1)).getTeacherByUsername("jane.smith");
            verify(studentService, never()).getStudentByUsername(anyString());
        }

        @Test
        @DisplayName("Should return error for unknown role")
        void getProfile_WithUnknownRole_ShouldReturnError() {
            // Arrange
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_UNKNOWN"));
            when(authentication.getAuthorities()).thenReturn(authorities);

            // Act
            ResponseEntity<ApiResponse<?>> response = profileController.getProfile(authentication);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertFalse(response.getBody().isSuccess());
            assertEquals("Unknown role", response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update student profile successfully")
        void updateProfile_AsStudent_ShouldReturnUpdatedProfile() {
            // Arrange
            when(authentication.getName()).thenReturn("john.doe");
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_STUDENT.name()));
            when(authentication.getAuthorities()).thenReturn(authorities);

            StudentResponse updatedResponse = StudentResponse.builder()
                    .id(1L)
                    .firstName("John Updated")
                    .lastName("Doe")
                    .address("456 New St")
                    .phoneNumber("9876543210")
                    .email("john.updated@email.com")
                    .build();
            when(studentService.updateStudentProfile(any(StudentUpdateRequest.class), eq("john.doe")))
                    .thenReturn(updatedResponse);

            // Act
            ResponseEntity<ApiResponse<?>> response =
                    profileController.updateProfile(studentUpdateRequest, authentication);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Profile updated successfully", response.getBody().getMessage());
            StudentResponse data = (StudentResponse) response.getBody().getData();
            assertEquals("John Updated", data.getFirstName());
            verify(studentService, times(1)).updateStudentProfile(any(StudentUpdateRequest.class), eq("john.doe"));
        }

        @Test
        @DisplayName("Should return error when teacher tries to update profile")
        void updateProfile_AsTeacher_ShouldReturnError() {
            // Arrange
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_TEACHER.name()));
            when(authentication.getAuthorities()).thenReturn(authorities);

            // Act
            ResponseEntity<ApiResponse<?>> response =
                    profileController.updateProfile(studentUpdateRequest, authentication);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertFalse(response.getBody().isSuccess());
            assertEquals("Profile update not supported for this role", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should return error for unknown role")
        void updateProfile_WithUnknownRole_ShouldReturnError() {
            // Arrange
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_UNKNOWN"));
            when(authentication.getAuthorities()).thenReturn(authorities);

            // Act
            ResponseEntity<ApiResponse<?>> response =
                    profileController.updateProfile(studentUpdateRequest, authentication);

            // Assert
            assertNotNull(response);
            assertFalse(response.getBody().isSuccess());
            assertEquals("Profile update not supported for this role", response.getBody().getMessage());
        }
    }
}

