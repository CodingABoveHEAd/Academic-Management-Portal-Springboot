package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.request.DepartmentRequest;
import com.niloy.student_portal.dto.response.ApiResponse;
import com.niloy.student_portal.dto.response.DepartmentResponse;
import com.niloy.student_portal.service.DepartmentService;
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
@DisplayName("DepartmentController Tests")
class DepartmentControllerTest {

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    private DepartmentRequest departmentRequest;
    private DepartmentResponse departmentResponse;

    @BeforeEach
    void setUp() {
        departmentRequest = new DepartmentRequest();
        departmentRequest.setName("Computer Science");
        departmentRequest.setDescription("Department of Computer Science and Engineering");

        departmentResponse = DepartmentResponse.builder()
                .id(1L)
                .name("Computer Science")
                .description("Department of Computer Science and Engineering")
                .teacherCount(5)
                .courseCount(10)
                .build();
    }

    @Nested
    @DisplayName("Create Department Tests")
    class CreateDepartmentTests {

        @Test
        @DisplayName("Should create department successfully")
        void createDepartment_WithValidRequest_ShouldReturnCreated() {
            // Arrange
            when(departmentService.createDepartment(any(DepartmentRequest.class))).thenReturn(departmentResponse);

            // Act
            ResponseEntity<ApiResponse<DepartmentResponse>> response =
                    departmentController.createDepartment(departmentRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Department created successfully", response.getBody().getMessage());
            assertEquals("Computer Science", response.getBody().getData().getName());
            verify(departmentService, times(1)).createDepartment(any(DepartmentRequest.class));
        }
    }

    @Nested
    @DisplayName("Get Department By ID Tests")
    class GetDepartmentByIdTests {

        @Test
        @DisplayName("Should return department when found")
        void getDepartmentById_WhenExists_ShouldReturnDepartment() {
            // Arrange
            when(departmentService.getDepartmentById(1L)).thenReturn(departmentResponse);

            // Act
            ResponseEntity<ApiResponse<DepartmentResponse>> response =
                    departmentController.getDepartmentById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Computer Science", response.getBody().getData().getName());
            verify(departmentService, times(1)).getDepartmentById(1L);
        }
    }

    @Nested
    @DisplayName("Get All Departments Tests")
    class GetAllDepartmentsTests {

        @Test
        @DisplayName("Should return all departments")
        void getAllDepartments_ShouldReturnList() {
            // Arrange
            DepartmentResponse dept2 = DepartmentResponse.builder()
                    .id(2L)
                    .name("Mathematics")
                    .description("Department of Mathematics")
                    .teacherCount(3)
                    .courseCount(8)
                    .build();
            List<DepartmentResponse> departments = Arrays.asList(departmentResponse, dept2);
            when(departmentService.getAllDepartments()).thenReturn(departments);

            // Act
            ResponseEntity<ApiResponse<List<DepartmentResponse>>> response =
                    departmentController.getAllDepartments();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(2, response.getBody().getData().size());
            verify(departmentService, times(1)).getAllDepartments();
        }

        @Test
        @DisplayName("Should return empty list when no departments exist")
        void getAllDepartments_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(departmentService.getAllDepartments()).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<ApiResponse<List<DepartmentResponse>>> response =
                    departmentController.getAllDepartments();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().getData().isEmpty());
        }
    }

    @Nested
    @DisplayName("Update Department Tests")
    class UpdateDepartmentTests {

        @Test
        @DisplayName("Should update department successfully")
        void updateDepartment_WithValidRequest_ShouldReturnUpdated() {
            // Arrange
            departmentRequest.setName("Updated Department");
            departmentResponse.setName("Updated Department");
            when(departmentService.updateDepartment(eq(1L), any(DepartmentRequest.class)))
                    .thenReturn(departmentResponse);

            // Act
            ResponseEntity<ApiResponse<DepartmentResponse>> response =
                    departmentController.updateDepartment(1L, departmentRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Department updated successfully", response.getBody().getMessage());
            assertEquals("Updated Department", response.getBody().getData().getName());
            verify(departmentService, times(1)).updateDepartment(eq(1L), any(DepartmentRequest.class));
        }
    }

    @Nested
    @DisplayName("Delete Department Tests")
    class DeleteDepartmentTests {

        @Test
        @DisplayName("Should delete department successfully")
        void deleteDepartment_WhenExists_ShouldReturnSuccess() {
            // Arrange
            doNothing().when(departmentService).deleteDepartment(1L);

            // Act
            ResponseEntity<ApiResponse<Void>> response = departmentController.deleteDepartment(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Department deleted successfully", response.getBody().getMessage());
            verify(departmentService, times(1)).deleteDepartment(1L);
        }
    }
}

