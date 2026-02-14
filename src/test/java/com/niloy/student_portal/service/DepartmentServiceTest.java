package com.niloy.student_portal.service;

import com.niloy.student_portal.dto.request.DepartmentRequest;
import com.niloy.student_portal.dto.response.DepartmentResponse;
import com.niloy.student_portal.entity.Department;
import com.niloy.student_portal.exception.DuplicateResourceException;
import com.niloy.student_portal.exception.ResourceNotFoundException;
import com.niloy.student_portal.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService Tests")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private DepartmentRequest departmentRequest;
    private Department department;

    @BeforeEach
    void setUp() {
        departmentRequest = new DepartmentRequest();
        departmentRequest.setName("Computer Science");
        departmentRequest.setDescription("Department of Computer Science and Engineering");

        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");
        department.setDescription("Department of Computer Science and Engineering");
        department.setTeachers(new ArrayList<>());
        department.setCourses(new ArrayList<>());
    }

    @Nested
    @DisplayName("Create Department Tests")
    class CreateDepartmentTests {

        @Test
        @DisplayName("Should create department successfully")
        void createDepartment_WithValidRequest_ShouldReturnDepartmentResponse() {
            // Arrange
            when(departmentRepository.existsByName("Computer Science")).thenReturn(false);
            when(departmentRepository.save(any(Department.class))).thenReturn(department);

            // Act
            DepartmentResponse response = departmentService.createDepartment(departmentRequest);

            // Assert
            assertNotNull(response);
            assertEquals("Computer Science", response.getName());
            assertEquals("Department of Computer Science and Engineering", response.getDescription());
            verify(departmentRepository, times(1)).existsByName("Computer Science");
            verify(departmentRepository, times(1)).save(any(Department.class));
        }

        @Test
        @DisplayName("Should throw exception when department name already exists")
        void createDepartment_WithDuplicateName_ShouldThrowException() {
            // Arrange
            when(departmentRepository.existsByName("Computer Science")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> departmentService.createDepartment(departmentRequest)
            );
            assertTrue(exception.getMessage().contains("Department"));
            verify(departmentRepository, times(1)).existsByName("Computer Science");
            verify(departmentRepository, never()).save(any(Department.class));
        }
    }

    @Nested
    @DisplayName("Get Department By ID Tests")
    class GetDepartmentByIdTests {

        @Test
        @DisplayName("Should return department when found")
        void getDepartmentById_WhenExists_ShouldReturnDepartmentResponse() {
            // Arrange
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

            // Act
            DepartmentResponse response = departmentService.getDepartmentById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Computer Science", response.getName());
            verify(departmentRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when department not found")
        void getDepartmentById_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> departmentService.getDepartmentById(1L)
            );
            assertTrue(exception.getMessage().contains("Department"));
            verify(departmentRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("Get All Departments Tests")
    class GetAllDepartmentsTests {

        @Test
        @DisplayName("Should return all departments")
        void getAllDepartments_ShouldReturnList() {
            // Arrange
            Department dept2 = new Department();
            dept2.setId(2L);
            dept2.setName("Mathematics");
            dept2.setDescription("Department of Mathematics");
            dept2.setTeachers(new ArrayList<>());
            dept2.setCourses(new ArrayList<>());

            List<Department> departments = Arrays.asList(department, dept2);
            when(departmentRepository.findAll()).thenReturn(departments);

            // Act
            List<DepartmentResponse> response = departmentService.getAllDepartments();

            // Assert
            assertNotNull(response);
            assertEquals(2, response.size());
            assertEquals("Computer Science", response.get(0).getName());
            assertEquals("Mathematics", response.get(1).getName());
            verify(departmentRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no departments exist")
        void getAllDepartments_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(departmentRepository.findAll()).thenReturn(new ArrayList<>());

            // Act
            List<DepartmentResponse> response = departmentService.getAllDepartments();

            // Assert
            assertNotNull(response);
            assertTrue(response.isEmpty());
            verify(departmentRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Update Department Tests")
    class UpdateDepartmentTests {

        @Test
        @DisplayName("Should update department successfully")
        void updateDepartment_WithValidRequest_ShouldReturnUpdatedDepartmentResponse() {
            // Arrange
            DepartmentRequest updateRequest = new DepartmentRequest();
            updateRequest.setName("Computer Science Updated");
            updateRequest.setDescription("Updated description");

            Department updatedDepartment = new Department();
            updatedDepartment.setId(1L);
            updatedDepartment.setName("Computer Science Updated");
            updatedDepartment.setDescription("Updated description");
            updatedDepartment.setTeachers(new ArrayList<>());
            updatedDepartment.setCourses(new ArrayList<>());

            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
            when(departmentRepository.existsByName("Computer Science Updated")).thenReturn(false);
            when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);

            // Act
            DepartmentResponse response = departmentService.updateDepartment(1L, updateRequest);

            // Assert
            assertNotNull(response);
            assertEquals("Computer Science Updated", response.getName());
            assertEquals("Updated description", response.getDescription());
            verify(departmentRepository, times(1)).findById(1L);
            verify(departmentRepository, times(1)).save(any(Department.class));
        }

        @Test
        @DisplayName("Should update department when name is unchanged")
        void updateDepartment_WithSameName_ShouldNotCheckDuplicate() {
            // Arrange
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
            when(departmentRepository.save(any(Department.class))).thenReturn(department);

            // Act
            DepartmentResponse response = departmentService.updateDepartment(1L, departmentRequest);

            // Assert
            assertNotNull(response);
            verify(departmentRepository, never()).existsByName(anyString());
        }

        @Test
        @DisplayName("Should throw exception when new name already exists")
        void updateDepartment_WithDuplicateName_ShouldThrowException() {
            // Arrange
            DepartmentRequest updateRequest = new DepartmentRequest();
            updateRequest.setName("Mathematics");
            updateRequest.setDescription("Description");

            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
            when(departmentRepository.existsByName("Mathematics")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> departmentService.updateDepartment(1L, updateRequest)
            );
            assertTrue(exception.getMessage().contains("Department"));
            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("Should throw exception when department not found")
        void updateDepartment_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> departmentService.updateDepartment(1L, departmentRequest)
            );
            assertTrue(exception.getMessage().contains("Department"));
        }
    }

    @Nested
    @DisplayName("Delete Department Tests")
    class DeleteDepartmentTests {

        @Test
        @DisplayName("Should delete department successfully")
        void deleteDepartment_WhenExists_ShouldDeleteSuccessfully() {
            // Arrange
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
            doNothing().when(departmentRepository).delete(department);

            // Act
            departmentService.deleteDepartment(1L);

            // Assert
            verify(departmentRepository, times(1)).findById(1L);
            verify(departmentRepository, times(1)).delete(department);
        }

        @Test
        @DisplayName("Should throw exception when department not found")
        void deleteDepartment_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> departmentService.deleteDepartment(1L)
            );
            assertTrue(exception.getMessage().contains("Department"));
            verify(departmentRepository, never()).delete(any(Department.class));
        }
    }

    @Nested
    @DisplayName("Get Department Entity Tests")
    class GetDepartmentEntityTests {

        @Test
        @DisplayName("Should return department entity when found")
        void getDepartmentEntity_WhenExists_ShouldReturnDepartment() {
            // Arrange
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

            // Act
            Department result = departmentService.getDepartmentEntity(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Computer Science", result.getName());
            verify(departmentRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when department entity not found")
        void getDepartmentEntity_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> departmentService.getDepartmentEntity(1L)
            );
            assertTrue(exception.getMessage().contains("Department"));
        }
    }
}

