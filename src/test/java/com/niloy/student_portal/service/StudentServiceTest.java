package com.niloy.student_portal.service;

import com.niloy.student_portal.dto.request.StudentCreateRequest;
import com.niloy.student_portal.dto.request.StudentUpdateRequest;
import com.niloy.student_portal.dto.response.StudentResponse;
import com.niloy.student_portal.entity.*;
import com.niloy.student_portal.exception.BadRequestException;
import com.niloy.student_portal.exception.DuplicateResourceException;
import com.niloy.student_portal.exception.ResourceNotFoundException;
import com.niloy.student_portal.exception.UnauthorizedAccessException;
import com.niloy.student_portal.repository.StudentRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Tests")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeacherService teacherService;

    @Mock
    private CourseService courseService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    private StudentCreateRequest studentCreateRequest;
    private StudentUpdateRequest studentUpdateRequest;
    private Student student;
    private User user;
    private Teacher teacher;
    private Course course;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");

        User teacherUser = new User();
        teacherUser.setId(1L);
        teacherUser.setUsername("teacher1");
        teacherUser.setEmail("teacher@email.com");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("Jane");
        teacher.setLastName("Smith");
        teacher.setEmployeeId("EMP001");
        teacher.setUser(teacherUser);
        teacher.setDepartment(department);
        teacher.setStudents(new ArrayList<>());

        user = new User();
        user.setId(2L);
        user.setUsername("john.doe");
        user.setPassword("encodedPassword");
        user.setEmail("john.doe@email.com");
        user.setRole(Role.ROLE_STUDENT);
        user.setEnabled(true);

        course = new Course();
        course.setId(1L);
        course.setCourseCode("CS101");
        course.setCourseName("Introduction to CS");
        course.setDepartment(department);
        course.setEnrolledStudents(new ArrayList<>());

        studentCreateRequest = new StudentCreateRequest();
        studentCreateRequest.setFirstName("John");
        studentCreateRequest.setLastName("Doe");
        studentCreateRequest.setStudentId("STU001");
        studentCreateRequest.setDateOfBirth(LocalDate.of(2000, 1, 15));
        studentCreateRequest.setAddress("123 Main St");
        studentCreateRequest.setPhoneNumber("1234567890");
        studentCreateRequest.setUsername("john.doe");
        studentCreateRequest.setPassword("password123");
        studentCreateRequest.setEmail("john.doe@email.com");

        studentUpdateRequest = new StudentUpdateRequest();
        studentUpdateRequest.setFirstName("John Updated");
        studentUpdateRequest.setLastName("Doe");
        studentUpdateRequest.setAddress("456 New St");
        studentUpdateRequest.setPhoneNumber("9876543210");
        studentUpdateRequest.setEmail("john.updated@email.com");

        student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setStudentId("STU001");
        student.setDateOfBirth(LocalDate.of(2000, 1, 15));
        student.setAddress("123 Main St");
        student.setPhoneNumber("1234567890");
        student.setUser(user);
        student.setTeacher(teacher);
        student.setEnrolledCourse(null);
    }

    @Nested
    @DisplayName("Create Student Tests")
    class CreateStudentTests {

        @Test
        @DisplayName("Should create student successfully")
        void createStudent_WithValidRequest_ShouldReturnStudentResponse() {
            // Arrange
            when(studentRepository.existsByStudentId("STU001")).thenReturn(false);
            when(userRepository.existsByUsername("john.doe")).thenReturn(false);
            when(userRepository.existsByEmail("john.doe@email.com")).thenReturn(false);
            when(teacherService.getTeacherEntityByUsername("teacher1")).thenReturn(teacher);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(studentRepository.save(any(Student.class))).thenReturn(student);

            // Act
            StudentResponse response = studentService.createStudent(studentCreateRequest, "teacher1");

            // Assert
            assertNotNull(response);
            assertEquals("John", response.getFirstName());
            assertEquals("Doe", response.getLastName());
            assertEquals("STU001", response.getStudentId());
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when student ID already exists")
        void createStudent_WithDuplicateStudentId_ShouldThrowException() {
            // Arrange
            when(studentRepository.existsByStudentId("STU001")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> studentService.createStudent(studentCreateRequest, "teacher1")
            );
            assertTrue(exception.getMessage().contains("Student"));
            verify(studentRepository, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void createStudent_WithDuplicateUsername_ShouldThrowException() {
            // Arrange
            when(studentRepository.existsByStudentId("STU001")).thenReturn(false);
            when(userRepository.existsByUsername("john.doe")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> studentService.createStudent(studentCreateRequest, "teacher1")
            );
            assertTrue(exception.getMessage().contains("User"));
            verify(studentRepository, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createStudent_WithDuplicateEmail_ShouldThrowException() {
            // Arrange
            when(studentRepository.existsByStudentId("STU001")).thenReturn(false);
            when(userRepository.existsByUsername("john.doe")).thenReturn(false);
            when(userRepository.existsByEmail("john.doe@email.com")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> studentService.createStudent(studentCreateRequest, "teacher1")
            );
            assertTrue(exception.getMessage().contains("User"));
            verify(studentRepository, never()).save(any(Student.class));
        }
    }

    @Nested
    @DisplayName("Get Student By ID Tests")
    class GetStudentByIdTests {

        @Test
        @DisplayName("Should return student when found")
        void getStudentById_WhenExists_ShouldReturnStudentResponse() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            // Act
            StudentResponse response = studentService.getStudentById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("John", response.getFirstName());
            assertEquals("STU001", response.getStudentId());
            verify(studentRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when student not found")
        void getStudentById_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> studentService.getStudentById(1L)
            );
            assertTrue(exception.getMessage().contains("Student"));
        }
    }

    @Nested
    @DisplayName("Get Student By Username Tests")
    class GetStudentByUsernameTests {

        @Test
        @DisplayName("Should return student when found by username")
        void getStudentByUsername_WhenExists_ShouldReturnStudentResponse() {
            // Arrange
            when(studentRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(student));

            // Act
            StudentResponse response = studentService.getStudentByUsername("john.doe");

            // Assert
            assertNotNull(response);
            assertEquals("John", response.getFirstName());
            assertEquals("john.doe@email.com", response.getEmail());
            verify(studentRepository, times(1)).findByUserUsername("john.doe");
        }

        @Test
        @DisplayName("Should throw exception when student not found by username")
        void getStudentByUsername_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(studentRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> studentService.getStudentByUsername("unknown")
            );
            assertTrue(exception.getMessage().contains("Student"));
        }
    }

    @Nested
    @DisplayName("Get All Students Tests")
    class GetAllStudentsTests {

        @Test
        @DisplayName("Should return all students")
        void getAllStudents_ShouldReturnList() {
            // Arrange
            Student student2 = new Student();
            student2.setId(2L);
            student2.setFirstName("Jane");
            student2.setLastName("Doe");
            student2.setStudentId("STU002");
            student2.setTeacher(teacher);
            User user2 = new User();
            user2.setEmail("jane@email.com");
            student2.setUser(user2);

            List<Student> students = Arrays.asList(student, student2);
            when(studentRepository.findAll()).thenReturn(students);

            // Act
            List<StudentResponse> response = studentService.getAllStudents();

            // Assert
            assertNotNull(response);
            assertEquals(2, response.size());
            assertEquals("John", response.get(0).getFirstName());
            assertEquals("Jane", response.get(1).getFirstName());
            verify(studentRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no students exist")
        void getAllStudents_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(studentRepository.findAll()).thenReturn(new ArrayList<>());

            // Act
            List<StudentResponse> response = studentService.getAllStudents();

            // Assert
            assertNotNull(response);
            assertTrue(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Students By Teacher Tests")
    class GetStudentsByTeacherTests {

        @Test
        @DisplayName("Should return students managed by teacher")
        void getStudentsByTeacher_ShouldReturnList() {
            // Arrange
            List<Student> students = Arrays.asList(student);
            when(teacherService.getTeacherEntityByUsername("teacher1")).thenReturn(teacher);
            when(studentRepository.findByTeacher(teacher)).thenReturn(students);

            // Act
            List<StudentResponse> response = studentService.getStudentsByTeacher("teacher1");

            // Assert
            assertNotNull(response);
            assertEquals(1, response.size());
            assertEquals("John", response.get(0).getFirstName());
            verify(studentRepository, times(1)).findByTeacher(teacher);
        }
    }

    @Nested
    @DisplayName("Get Students By Course Tests")
    class GetStudentsByCourseTests {

        @Test
        @DisplayName("Should return students enrolled in course")
        void getStudentsByCourse_ShouldReturnList() {
            // Arrange
            student.setEnrolledCourse(course);
            List<Student> students = Arrays.asList(student);
            when(studentRepository.findByEnrolledCourseId(1L)).thenReturn(students);

            // Act
            List<StudentResponse> response = studentService.getStudentsByCourse(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.size());
            assertEquals("Introduction to CS", response.get(0).getEnrolledCourseName());
            verify(studentRepository, times(1)).findByEnrolledCourseId(1L);
        }
    }

    @Nested
    @DisplayName("Update Student By Teacher Tests")
    class UpdateStudentByTeacherTests {

        @Test
        @DisplayName("Should update student successfully")
        void updateStudentByTeacher_WithValidRequest_ShouldReturnUpdatedStudent() {
            // Arrange
            StudentCreateRequest updateRequest = new StudentCreateRequest();
            updateRequest.setFirstName("John Updated");
            updateRequest.setLastName("Doe Updated");
            updateRequest.setStudentId("STU001");
            updateRequest.setEmail("john.updated@email.com");

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(teacherService.getTeacherEntityByUsername("teacher1")).thenReturn(teacher);
            when(studentRepository.save(any(Student.class))).thenReturn(student);

            // Act
            StudentResponse response = studentService.updateStudentByTeacher(1L, updateRequest, "teacher1");

            // Assert
            assertNotNull(response);
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when teacher does not manage student")
        void updateStudentByTeacher_WhenUnauthorized_ShouldThrowException() {
            // Arrange
            Teacher otherTeacher = new Teacher();
            otherTeacher.setId(2L);

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(teacherService.getTeacherEntityByUsername("teacher2")).thenReturn(otherTeacher);

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> studentService.updateStudentByTeacher(1L, studentCreateRequest, "teacher2")
            );
            assertTrue(exception.getMessage().contains("not authorized"));
            verify(studentRepository, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when new student ID already exists")
        void updateStudentByTeacher_WithDuplicateStudentId_ShouldThrowException() {
            // Arrange
            StudentCreateRequest updateRequest = new StudentCreateRequest();
            updateRequest.setStudentId("STU002");

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(teacherService.getTeacherEntityByUsername("teacher1")).thenReturn(teacher);
            when(studentRepository.existsByStudentId("STU002")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> studentService.updateStudentByTeacher(1L, updateRequest, "teacher1")
            );
            assertTrue(exception.getMessage().contains("Student"));
        }
    }

    @Nested
    @DisplayName("Update Student Profile Tests")
    class UpdateStudentProfileTests {

        @Test
        @DisplayName("Should update student profile successfully")
        void updateStudentProfile_WithValidRequest_ShouldReturnUpdatedStudent() {
            // Arrange
            when(studentRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(student));
            when(studentRepository.save(any(Student.class))).thenReturn(student);

            // Act
            StudentResponse response = studentService.updateStudentProfile(studentUpdateRequest, "john.doe");

            // Assert
            assertNotNull(response);
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void updateStudentProfile_WithDuplicateEmail_ShouldThrowException() {
            // Arrange
            studentUpdateRequest.setEmail("existing@email.com");
            when(studentRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(student));
            when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> studentService.updateStudentProfile(studentUpdateRequest, "john.doe")
            );
            assertTrue(exception.getMessage().contains("User"));
        }

        @Test
        @DisplayName("Should throw exception when student not found")
        void updateStudentProfile_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(studentRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> studentService.updateStudentProfile(studentUpdateRequest, "unknown")
            );
        }
    }

    @Nested
    @DisplayName("Delete Student Tests")
    class DeleteStudentTests {

        @Test
        @DisplayName("Should delete student successfully")
        void deleteStudent_WhenAuthorized_ShouldDeleteSuccessfully() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(teacherService.getTeacherEntityByUsername("teacher1")).thenReturn(teacher);
            doNothing().when(studentRepository).delete(student);

            // Act
            studentService.deleteStudent(1L, "teacher1");

            // Assert
            verify(studentRepository, times(1)).delete(student);
        }

        @Test
        @DisplayName("Should throw exception when teacher does not manage student")
        void deleteStudent_WhenUnauthorized_ShouldThrowException() {
            // Arrange
            Teacher otherTeacher = new Teacher();
            otherTeacher.setId(2L);

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(teacherService.getTeacherEntityByUsername("teacher2")).thenReturn(otherTeacher);

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> studentService.deleteStudent(1L, "teacher2")
            );
            assertTrue(exception.getMessage().contains("not authorized"));
            verify(studentRepository, never()).delete(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when student not found")
        void deleteStudent_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> studentService.deleteStudent(1L, "teacher1")
            );
        }
    }

    @Nested
    @DisplayName("Enroll In Course Tests")
    class EnrollInCourseTests {

        @Test
        @DisplayName("Should enroll in course successfully")
        void enrollInCourse_WhenNotEnrolled_ShouldReturnUpdatedStudent() {
            // Arrange
            when(studentRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(student));
            when(courseService.getCourseEntity(1L)).thenReturn(course);

            Student enrolledStudent = new Student();
            enrolledStudent.setId(1L);
            enrolledStudent.setFirstName("John");
            enrolledStudent.setLastName("Doe");
            enrolledStudent.setStudentId("STU001");
            enrolledStudent.setUser(user);
            enrolledStudent.setTeacher(teacher);
            enrolledStudent.setEnrolledCourse(course);

            when(studentRepository.save(any(Student.class))).thenReturn(enrolledStudent);

            // Act
            StudentResponse response = studentService.enrollInCourse(1L, "john.doe");

            // Assert
            assertNotNull(response);
            assertEquals("Introduction to CS", response.getEnrolledCourseName());
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when already enrolled")
        void enrollInCourse_WhenAlreadyEnrolled_ShouldThrowException() {
            // Arrange
            student.setEnrolledCourse(course);
            when(studentRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(student));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> studentService.enrollInCourse(2L, "john.doe")
            );
            assertTrue(exception.getMessage().contains("already enrolled"));
            verify(studentRepository, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when student not found")
        void enrollInCourse_WhenStudentNotFound_ShouldThrowException() {
            // Arrange
            when(studentRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> studentService.enrollInCourse(1L, "unknown")
            );
        }
    }

    @Nested
    @DisplayName("Drop Course Tests")
    class DropCourseTests {

        @Test
        @DisplayName("Should drop course successfully")
        void dropCourse_WhenEnrolled_ShouldReturnUpdatedStudent() {
            // Arrange
            student.setEnrolledCourse(course);
            when(studentRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(student));

            Student droppedStudent = new Student();
            droppedStudent.setId(1L);
            droppedStudent.setFirstName("John");
            droppedStudent.setLastName("Doe");
            droppedStudent.setStudentId("STU001");
            droppedStudent.setUser(user);
            droppedStudent.setTeacher(teacher);
            droppedStudent.setEnrolledCourse(null);

            when(studentRepository.save(any(Student.class))).thenReturn(droppedStudent);

            // Act
            StudentResponse response = studentService.dropCourse("john.doe");

            // Assert
            assertNotNull(response);
            assertNull(response.getEnrolledCourseName());
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when not enrolled")
        void dropCourse_WhenNotEnrolled_ShouldThrowException() {
            // Arrange
            student.setEnrolledCourse(null);
            when(studentRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(student));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> studentService.dropCourse("john.doe")
            );
            assertTrue(exception.getMessage().contains("not enrolled"));
            verify(studentRepository, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when student not found")
        void dropCourse_WhenStudentNotFound_ShouldThrowException() {
            // Arrange
            when(studentRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> studentService.dropCourse("unknown")
            );
        }
    }

    @Nested
    @DisplayName("Get Student Entity Tests")
    class GetStudentEntityTests {

        @Test
        @DisplayName("Should return student entity when found")
        void getStudentEntity_WhenExists_ShouldReturnStudent() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            // Act
            Student result = studentService.getStudentEntity(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("John", result.getFirstName());
        }

        @Test
        @DisplayName("Should throw exception when student entity not found")
        void getStudentEntity_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> studentService.getStudentEntity(1L)
            );
        }
    }
}

