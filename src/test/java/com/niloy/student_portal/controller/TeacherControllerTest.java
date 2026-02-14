package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.request.TeacherRequest;
import com.niloy.student_portal.dto.response.ApiResponse;
import com.niloy.student_portal.dto.response.TeacherResponse;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherController Tests")
class TeacherControllerTest {

    @Mock
    private TeacherService teacherService;

    @InjectMocks
    private TeacherController teacherController;

    private TeacherRequest teacherRequest;
    private TeacherResponse teacherResponse;

    @BeforeEach
    void setUp() {
        teacherRequest = new TeacherRequest();
        teacherRequest.setFirstName("Jane");
        teacherRequest.setLastName("Smith");
        teacherRequest.setEmployeeId("EMP001");
        teacherRequest.setSpecialization("Computer Science");
        teacherRequest.setDepartmentId(1L);
        teacherRequest.setUsername("jane.smith");
        teacherRequest.setPassword("password123");
        teacherRequest.setEmail("jane.smith@email.com");

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
    }

    @Nested
    @DisplayName("Create Teacher Tests")
    class CreateTeacherTests {

        @Test
        @DisplayName("Should create teacher successfully")
        void createTeacher_WithValidRequest_ShouldReturnCreated() {
            // Arrange
            when(teacherService.createTeacher(any(TeacherRequest.class))).thenReturn(teacherResponse);

            // Act
            ResponseEntity<ApiResponse<TeacherResponse>> response =
                    teacherController.createTeacher(teacherRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Teacher created successfully", response.getBody().getMessage());
            assertEquals("Jane", response.getBody().getData().getFirstName());
            assertEquals("EMP001", response.getBody().getData().getEmployeeId());
            verify(teacherService, times(1)).createTeacher(any(TeacherRequest.class));
        }
    }

    @Nested
    @DisplayName("Get Teacher By ID Tests")
    class GetTeacherByIdTests {

        @Test
        @DisplayName("Should return teacher when found")
        void getTeacherById_WhenExists_ShouldReturnTeacher() {
            // Arrange
            when(teacherService.getTeacherById(1L)).thenReturn(teacherResponse);

            // Act
            ResponseEntity<ApiResponse<TeacherResponse>> response = teacherController.getTeacherById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Jane", response.getBody().getData().getFirstName());
            verify(teacherService, times(1)).getTeacherById(1L);
        }
    }

    @Nested
    @DisplayName("Get All Teachers Tests")
    class GetAllTeachersTests {

        @Test
        @DisplayName("Should return all teachers")
        void getAllTeachers_ShouldReturnList() {
            // Arrange
            TeacherResponse teacher2 = TeacherResponse.builder()
                    .id(2L)
                    .firstName("John")
                    .lastName("Doe")
                    .employeeId("EMP002")
                    .specialization("Mathematics")
                    .build();
            List<TeacherResponse> teachers = Arrays.asList(teacherResponse, teacher2);
            when(teacherService.getAllTeachers()).thenReturn(teachers);

            // Act
            ResponseEntity<ApiResponse<List<TeacherResponse>>> response = teacherController.getAllTeachers();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(2, response.getBody().getData().size());
            verify(teacherService, times(1)).getAllTeachers();
        }

        @Test
        @DisplayName("Should return empty list when no teachers exist")
        void getAllTeachers_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(teacherService.getAllTeachers()).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<ApiResponse<List<TeacherResponse>>> response = teacherController.getAllTeachers();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().getData().isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Teachers By Department Tests")
    class GetTeachersByDepartmentTests {

        @Test
        @DisplayName("Should return teachers for department")
        void getTeachersByDepartment_ShouldReturnList() {
            // Arrange
            List<TeacherResponse> teachers = Arrays.asList(teacherResponse);
            when(teacherService.getTeachersByDepartment(1L)).thenReturn(teachers);

            // Act
            ResponseEntity<ApiResponse<List<TeacherResponse>>> response =
                    teacherController.getTeachersByDepartment(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().size());
            verify(teacherService, times(1)).getTeachersByDepartment(1L);
        }

        @Test
        @DisplayName("Should return empty list when no teachers in department")
        void getTeachersByDepartment_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(teacherService.getTeachersByDepartment(1L)).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<ApiResponse<List<TeacherResponse>>> response =
                    teacherController.getTeachersByDepartment(1L);

            // Assert
            assertNotNull(response);
            assertTrue(response.getBody().getData().isEmpty());
        }
    }

    @Nested
    @DisplayName("Update Teacher Tests")
    class UpdateTeacherTests {

        @Test
        @DisplayName("Should update teacher successfully")
        void updateTeacher_WithValidRequest_ShouldReturnUpdated() {
            // Arrange
            teacherRequest.setFirstName("Jane Updated");
            teacherResponse.setFirstName("Jane Updated");
            when(teacherService.updateTeacher(eq(1L), any(TeacherRequest.class))).thenReturn(teacherResponse);

            // Act
            ResponseEntity<ApiResponse<TeacherResponse>> response =
                    teacherController.updateTeacher(1L, teacherRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Teacher updated successfully", response.getBody().getMessage());
            assertEquals("Jane Updated", response.getBody().getData().getFirstName());
            verify(teacherService, times(1)).updateTeacher(eq(1L), any(TeacherRequest.class));
        }
    }

    @Nested
    @DisplayName("Delete Teacher Tests")
    class DeleteTeacherTests {

        @Test
        @DisplayName("Should delete teacher successfully")
        void deleteTeacher_WhenExists_ShouldReturnSuccess() {
            // Arrange
            doNothing().when(teacherService).deleteTeacher(1L);

            // Act
            ResponseEntity<ApiResponse<Void>> response = teacherController.deleteTeacher(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Teacher deleted successfully", response.getBody().getMessage());
            verify(teacherService, times(1)).deleteTeacher(1L);
        }
    }
}

