package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.request.EnrollmentRequest;
import com.niloy.student_portal.dto.response.ApiResponse;
import com.niloy.student_portal.dto.response.StudentResponse;
import com.niloy.student_portal.service.StudentService;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentController Tests")
class EnrollmentControllerTest {

    @Mock
    private StudentService studentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EnrollmentController enrollmentController;

    private StudentResponse studentResponse;
    private EnrollmentRequest enrollmentRequest;

    @BeforeEach
    void setUp() {
        studentResponse = StudentResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .studentId("STU001")
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .email("john.doe@email.com")
                .teacherName("Jane Smith")
                .teacherId(1L)
                .build();

        enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setCourseId(1L);
    }

    @Nested
    @DisplayName("Enroll In Course Tests")
    class EnrollInCourseTests {

        @Test
        @DisplayName("Should enroll in course successfully")
        void enrollInCourse_WithValidRequest_ShouldReturnSuccess() {
            when(authentication.getName()).thenReturn("john.doe");
            studentResponse.setEnrolledCourseName("Introduction to CS");
            studentResponse.setEnrolledCourseId(1L);
            when(studentService.enrollInCourse(eq(1L), eq("john.doe"))).thenReturn(studentResponse);

            ResponseEntity<ApiResponse<StudentResponse>> response =
                    enrollmentController.enrollInCourse(enrollmentRequest, authentication);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Successfully enrolled in course", response.getBody().getMessage());
            verify(studentService, times(1)).enrollInCourse(eq(1L), eq("john.doe"));
        }
    }

    @Nested
    @DisplayName("Drop Course Tests")
    class DropCourseTests {

        @Test
        @DisplayName("Should drop course successfully")
        void dropCourse_WhenEnrolled_ShouldReturnSuccess() {
            when(authentication.getName()).thenReturn("john.doe");
            when(studentService.dropCourse("john.doe")).thenReturn(studentResponse);

            ResponseEntity<ApiResponse<StudentResponse>> response =
                    enrollmentController.dropCourse(authentication);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Successfully dropped course", response.getBody().getMessage());
            verify(studentService, times(1)).dropCourse("john.doe");
        }
    }

    @Nested
    @DisplayName("Get Enrollment Status Tests")
    class GetEnrollmentStatusTests {

        @Test
        @DisplayName("Should return enrollment status")
        void getEnrollmentStatus_ShouldReturnStatus() {
            when(authentication.getName()).thenReturn("john.doe");
            when(studentService.getStudentByUsername("john.doe")).thenReturn(studentResponse);

            ResponseEntity<ApiResponse<StudentResponse>> response =
                    enrollmentController.getEnrollmentStatus(authentication);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            verify(studentService, times(1)).getStudentByUsername("john.doe");
        }
    }
}

