package com.niloy.student_portal.service;

import com.niloy.student_portal.dto.request.TeacherRequest;
import com.niloy.student_portal.dto.response.TeacherResponse;
import com.niloy.student_portal.entity.Department;
import com.niloy.student_portal.entity.Role;
import com.niloy.student_portal.entity.Teacher;
import com.niloy.student_portal.entity.User;
import com.niloy.student_portal.exception.DuplicateResourceException;
import com.niloy.student_portal.exception.ResourceNotFoundException;
import com.niloy.student_portal.repository.TeacherRepository;
import com.niloy.student_portal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService Tests")
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TeacherService teacherService;

    private TeacherRequest teacherRequest;
    private Teacher teacher;
    private User user;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");
        department.setDescription("CS Department");

        user = new User();
        user.setId(1L);
        user.setUsername("jane.smith");
        user.setPassword("encodedPassword");
        user.setEmail("jane.smith@email.com");
        user.setRole(Role.ROLE_TEACHER);
        user.setEnabled(true);

        teacherRequest = new TeacherRequest();
        teacherRequest.setFirstName("Jane");
        teacherRequest.setLastName("Smith");
        teacherRequest.setEmployeeId("EMP001");
        teacherRequest.setSpecialization("Computer Science");
        teacherRequest.setDepartmentId(1L);
        teacherRequest.setUsername("jane.smith");
        teacherRequest.setPassword("password123");
        teacherRequest.setEmail("jane.smith@email.com");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("Jane");
        teacher.setLastName("Smith");
        teacher.setEmployeeId("EMP001");
        teacher.setSpecialization("Computer Science");
        teacher.setDepartment(department);
        teacher.setUser(user);
        teacher.setStudents(new ArrayList<>());
    }

    @Nested
    @DisplayName("Create Teacher Tests")
    class CreateTeacherTests {

        @Test
        @DisplayName("Should create teacher successfully")
        void createTeacher_WithValidRequest_ShouldReturnTeacherResponse() {
            // Arrange
            when(teacherRepository.existsByEmployeeId("EMP001")).thenReturn(false);
            when(userRepository.existsByUsername("jane.smith")).thenReturn(false);
            when(userRepository.existsByEmail("jane.smith@email.com")).thenReturn(false);
            when(departmentService.getDepartmentEntity(1L)).thenReturn(department);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

            // Act
            TeacherResponse response = teacherService.createTeacher(teacherRequest);

            // Assert
            assertNotNull(response);
            assertEquals("Jane", response.getFirstName());
            assertEquals("Smith", response.getLastName());
            assertEquals("EMP001", response.getEmployeeId());
            assertEquals("Computer Science", response.getDepartmentName());
            verify(teacherRepository, times(1)).save(any(Teacher.class));
        }

        @Test
        @DisplayName("Should throw exception when employee ID already exists")
        void createTeacher_WithDuplicateEmployeeId_ShouldThrowException() {
            // Arrange
            when(teacherRepository.existsByEmployeeId("EMP001")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> teacherService.createTeacher(teacherRequest)
            );
            assertTrue(exception.getMessage().contains("Teacher"));
            verify(teacherRepository, never()).save(any(Teacher.class));
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void createTeacher_WithDuplicateUsername_ShouldThrowException() {
            // Arrange
            when(teacherRepository.existsByEmployeeId("EMP001")).thenReturn(false);
            when(userRepository.existsByUsername("jane.smith")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> teacherService.createTeacher(teacherRequest)
            );
            assertTrue(exception.getMessage().contains("User"));
            verify(teacherRepository, never()).save(any(Teacher.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createTeacher_WithDuplicateEmail_ShouldThrowException() {
            // Arrange
            when(teacherRepository.existsByEmployeeId("EMP001")).thenReturn(false);
            when(userRepository.existsByUsername("jane.smith")).thenReturn(false);
            when(userRepository.existsByEmail("jane.smith@email.com")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> teacherService.createTeacher(teacherRequest)
            );
            assertTrue(exception.getMessage().contains("User"));
            verify(teacherRepository, never()).save(any(Teacher.class));
        }
    }

    @Nested
    @DisplayName("Get Teacher By ID Tests")
    class GetTeacherByIdTests {

        @Test
        @DisplayName("Should return teacher when found")
        void getTeacherById_WhenExists_ShouldReturnTeacherResponse() {
            // Arrange
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

            // Act
            TeacherResponse response = teacherService.getTeacherById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Jane", response.getFirstName());
            assertEquals("EMP001", response.getEmployeeId());
            verify(teacherRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when teacher not found")
        void getTeacherById_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> teacherService.getTeacherById(1L)
            );
            assertTrue(exception.getMessage().contains("Teacher"));
            verify(teacherRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("Get Teacher By Username Tests")
    class GetTeacherByUsernameTests {

        @Test
        @DisplayName("Should return teacher when found by username")
        void getTeacherByUsername_WhenExists_ShouldReturnTeacherResponse() {
            // Arrange
            when(teacherRepository.findByUserUsername("jane.smith")).thenReturn(Optional.of(teacher));

            // Act
            TeacherResponse response = teacherService.getTeacherByUsername("jane.smith");

            // Assert
            assertNotNull(response);
            assertEquals("Jane", response.getFirstName());
            assertEquals("jane.smith@email.com", response.getEmail());
            verify(teacherRepository, times(1)).findByUserUsername("jane.smith");
        }

        @Test
        @DisplayName("Should throw exception when teacher not found by username")
        void getTeacherByUsername_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(teacherRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> teacherService.getTeacherByUsername("unknown")
            );
            assertTrue(exception.getMessage().contains("Teacher"));
        }
    }

    @Nested
    @DisplayName("Get All Teachers Tests")
    class GetAllTeachersTests {

        @Test
        @DisplayName("Should return all teachers")
        void getAllTeachers_ShouldReturnList() {
            // Arrange
            Teacher teacher2 = new Teacher();
            teacher2.setId(2L);
            teacher2.setFirstName("John");
            teacher2.setLastName("Doe");
            teacher2.setEmployeeId("EMP002");
            teacher2.setDepartment(department);
            User user2 = new User();
            user2.setEmail("john@email.com");
            teacher2.setUser(user2);
            teacher2.setStudents(new ArrayList<>());

            List<Teacher> teachers = Arrays.asList(teacher, teacher2);
            when(teacherRepository.findAll()).thenReturn(teachers);

            // Act
            List<TeacherResponse> response = teacherService.getAllTeachers();

            // Assert
            assertNotNull(response);
            assertEquals(2, response.size());
            assertEquals("Jane", response.get(0).getFirstName());
            assertEquals("John", response.get(1).getFirstName());
            verify(teacherRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no teachers exist")
        void getAllTeachers_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(teacherRepository.findAll()).thenReturn(new ArrayList<>());

            // Act
            List<TeacherResponse> response = teacherService.getAllTeachers();

            // Assert
            assertNotNull(response);
            assertTrue(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Teachers By Department Tests")
    class GetTeachersByDepartmentTests {

        @Test
        @DisplayName("Should return teachers for department")
        void getTeachersByDepartment_ShouldReturnList() {
            // Arrange
            List<Teacher> teachers = Arrays.asList(teacher);
            when(teacherRepository.findByDepartmentId(1L)).thenReturn(teachers);

            // Act
            List<TeacherResponse> response = teacherService.getTeachersByDepartment(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.size());
            assertEquals("Jane", response.get(0).getFirstName());
            verify(teacherRepository, times(1)).findByDepartmentId(1L);
        }

        @Test
        @DisplayName("Should return empty list when no teachers in department")
        void getTeachersByDepartment_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(teacherRepository.findByDepartmentId(1L)).thenReturn(new ArrayList<>());

            // Act
            List<TeacherResponse> response = teacherService.getTeachersByDepartment(1L);

            // Assert
            assertNotNull(response);
            assertTrue(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("Update Teacher Tests")
    class UpdateTeacherTests {

        @Test
        @DisplayName("Should update teacher successfully")
        void updateTeacher_WithValidRequest_ShouldReturnUpdatedTeacherResponse() {
            // Arrange
            TeacherRequest updateRequest = new TeacherRequest();
            updateRequest.setFirstName("Jane Updated");
            updateRequest.setLastName("Smith Updated");
            updateRequest.setEmployeeId("EMP001");
            updateRequest.setSpecialization("AI");
            updateRequest.setDepartmentId(1L);
            updateRequest.setEmail("jane.updated@email.com");

            Teacher updatedTeacher = new Teacher();
            updatedTeacher.setId(1L);
            updatedTeacher.setFirstName("Jane Updated");
            updatedTeacher.setLastName("Smith Updated");
            updatedTeacher.setEmployeeId("EMP001");
            updatedTeacher.setSpecialization("AI");
            updatedTeacher.setDepartment(department);
            updatedTeacher.setUser(user);
            updatedTeacher.setStudents(new ArrayList<>());

            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
            when(departmentService.getDepartmentEntity(1L)).thenReturn(department);
            when(teacherRepository.save(any(Teacher.class))).thenReturn(updatedTeacher);

            // Act
            TeacherResponse response = teacherService.updateTeacher(1L, updateRequest);

            // Assert
            assertNotNull(response);
            assertEquals("Jane Updated", response.getFirstName());
            assertEquals("AI", response.getSpecialization());
            verify(teacherRepository, times(1)).save(any(Teacher.class));
        }

        @Test
        @DisplayName("Should throw exception when new employee ID already exists")
        void updateTeacher_WithDuplicateEmployeeId_ShouldThrowException() {
            // Arrange
            TeacherRequest updateRequest = new TeacherRequest();
            updateRequest.setEmployeeId("EMP002");
            updateRequest.setDepartmentId(1L);

            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
            when(teacherRepository.existsByEmployeeId("EMP002")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> teacherService.updateTeacher(1L, updateRequest)
            );
            assertTrue(exception.getMessage().contains("Teacher"));
            verify(teacherRepository, never()).save(any(Teacher.class));
        }

        @Test
        @DisplayName("Should throw exception when teacher not found")
        void updateTeacher_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> teacherService.updateTeacher(1L, teacherRequest)
            );
            assertTrue(exception.getMessage().contains("Teacher"));
        }
    }

    @Nested
    @DisplayName("Delete Teacher Tests")
    class DeleteTeacherTests {

        @Test
        @DisplayName("Should delete teacher successfully")
        void deleteTeacher_WhenExists_ShouldDeleteSuccessfully() {
            // Arrange
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
            doNothing().when(teacherRepository).delete(teacher);

            // Act
            teacherService.deleteTeacher(1L);

            // Assert
            verify(teacherRepository, times(1)).findById(1L);
            verify(teacherRepository, times(1)).delete(teacher);
        }

        @Test
        @DisplayName("Should throw exception when teacher not found")
        void deleteTeacher_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> teacherService.deleteTeacher(1L)
            );
            assertTrue(exception.getMessage().contains("Teacher"));
            verify(teacherRepository, never()).delete(any(Teacher.class));
        }
    }

    @Nested
    @DisplayName("Get Teacher Entity Tests")
    class GetTeacherEntityTests {

        @Test
        @DisplayName("Should return teacher entity when found")
        void getTeacherEntity_WhenExists_ShouldReturnTeacher() {
            // Arrange
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

            // Act
            Teacher result = teacherService.getTeacherEntity(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Jane", result.getFirstName());
        }

        @Test
        @DisplayName("Should throw exception when teacher entity not found")
        void getTeacherEntity_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teacherService.getTeacherEntity(1L)
            );
        }

        @Test
        @DisplayName("Should return teacher entity by username when found")
        void getTeacherEntityByUsername_WhenExists_ShouldReturnTeacher() {
            // Arrange
            when(teacherRepository.findByUserUsername("jane.smith")).thenReturn(Optional.of(teacher));

            // Act
            Teacher result = teacherService.getTeacherEntityByUsername("jane.smith");

            // Assert
            assertNotNull(result);
            assertEquals("Jane", result.getFirstName());
        }

        @Test
        @DisplayName("Should throw exception when teacher entity not found by username")
        void getTeacherEntityByUsername_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(teacherRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teacherService.getTeacherEntityByUsername("unknown")
            );
        }
    }
}

