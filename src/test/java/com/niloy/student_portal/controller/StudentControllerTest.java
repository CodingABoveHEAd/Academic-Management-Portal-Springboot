package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.request.StudentCreateRequest;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentController Tests")
class StudentControllerTest {

    @Mock
    private StudentService studentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StudentController studentController;

    private StudentCreateRequest studentCreateRequest;
    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
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
                .enrolledCourseName(null)
                .enrolledCourseId(null)
                .build();
    }

    @Nested
    @DisplayName("Create Student Tests")
    class CreateStudentTests {

        @Test
        @DisplayName("Should create student successfully")
        void createStudent_WithValidRequest_ShouldReturnCreated() {
            when(authentication.getName()).thenReturn("teacher1");
            when(studentService.createStudent(any(StudentCreateRequest.class), eq("teacher1")))
                    .thenReturn(studentResponse);

            ResponseEntity<ApiResponse<StudentResponse>> response =
                    studentController.createStudent(studentCreateRequest, authentication);

            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Student created successfully", response.getBody().getMessage());
            assertEquals("John", response.getBody().getData().getFirstName());
            assertEquals("STU001", response.getBody().getData().getStudentId());
            verify(studentService, times(1)).createStudent(any(StudentCreateRequest.class), eq("teacher1"));
        }
    }

    @Nested
    @DisplayName("Get Student By ID Tests")
    class GetStudentByIdTests {

        @Test
        @DisplayName("Should return student when found")
        void getStudentById_WhenExists_ShouldReturnStudent() {
            when(studentService.getStudentById(1L)).thenReturn(studentResponse);

            ResponseEntity<ApiResponse<StudentResponse>> response = studentController.getStudentById(1L);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals("John", response.getBody().getData().getFirstName());
            verify(studentService, times(1)).getStudentById(1L);
        }
    }

    @Nested
    @DisplayName("Get All Students Tests")
    class GetAllStudentsTests {

        @Test
        @DisplayName("Should return all students")
        void getAllStudents_ShouldReturnList() {
            StudentResponse student2 = StudentResponse.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .studentId("STU002")
                    .build();
            List<StudentResponse> students = Arrays.asList(studentResponse, student2);
            when(studentService.getAllStudents()).thenReturn(students);

            ResponseEntity<ApiResponse<List<StudentResponse>>> response = studentController.getAllStudents();

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals(2, response.getBody().getData().size());
            verify(studentService, times(1)).getAllStudents();
        }

        @Test
        @DisplayName("Should return empty list when no students exist")
        void getAllStudents_WhenEmpty_ShouldReturnEmptyList() {
            when(studentService.getAllStudents()).thenReturn(Collections.emptyList());

            ResponseEntity<ApiResponse<List<StudentResponse>>> response = studentController.getAllStudents();

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getData().isEmpty());
        }
    }

    @Nested
    @DisplayName("Get My Students Tests")
    class GetMyStudentsTests {

        @Test
        @DisplayName("Should return students managed by teacher")
        void getMyStudents_ShouldReturnTeacherStudents() {
            when(authentication.getName()).thenReturn("teacher1");
            List<StudentResponse> students = Arrays.asList(studentResponse);
            when(studentService.getStudentsByTeacher("teacher1")).thenReturn(students);

            ResponseEntity<ApiResponse<List<StudentResponse>>> response =
                    studentController.getMyStudents(authentication);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().size());
            verify(studentService, times(1)).getStudentsByTeacher("teacher1");
        }
    }

    @Nested
    @DisplayName("Get Students By Course Tests")
    class GetStudentsByCourseTests {

        @Test
        @DisplayName("Should return students enrolled in course")
        void getStudentsByCourse_ShouldReturnEnrolledStudents() {
            studentResponse.setEnrolledCourseName("Introduction to CS");
            studentResponse.setEnrolledCourseId(1L);
            List<StudentResponse> students = Arrays.asList(studentResponse);
            when(studentService.getStudentsByCourse(1L)).thenReturn(students);

            ResponseEntity<ApiResponse<List<StudentResponse>>> response =
                    studentController.getStudentsByCourse(1L);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().size());
            verify(studentService, times(1)).getStudentsByCourse(1L);
        }

        @Test
        @DisplayName("Should return empty list when no students enrolled")
        void getStudentsByCourse_WhenNoneEnrolled_ShouldReturnEmptyList() {
            when(studentService.getStudentsByCourse(1L)).thenReturn(Collections.emptyList());

            ResponseEntity<ApiResponse<List<StudentResponse>>> response =
                    studentController.getStudentsByCourse(1L);

            assertNotNull(response);
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getData().isEmpty());
        }
    }

    @Nested
    @DisplayName("Update Student By Teacher Tests")
    class UpdateStudentByTeacherTests {

        @Test
        @DisplayName("Should update student successfully")
        void updateStudentByTeacher_WithValidRequest_ShouldReturnUpdated() {
            when(authentication.getName()).thenReturn("teacher1");
            studentCreateRequest.setFirstName("John Updated");
            studentResponse.setFirstName("John Updated");
            when(studentService.updateStudentByTeacher(eq(1L), any(StudentCreateRequest.class), eq("teacher1")))
                    .thenReturn(studentResponse);

            ResponseEntity<ApiResponse<StudentResponse>> response =
                    studentController.updateStudentByTeacher(1L, studentCreateRequest, authentication);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Student updated successfully", response.getBody().getMessage());
            assertEquals("John Updated", response.getBody().getData().getFirstName());
            verify(studentService, times(1))
                    .updateStudentByTeacher(eq(1L), any(StudentCreateRequest.class), eq("teacher1"));
        }
    }

    @Nested
    @DisplayName("Delete Student Tests")
    class DeleteStudentTests {

        @Test
        @DisplayName("Should delete student successfully")
        void deleteStudent_WhenAuthorized_ShouldReturnSuccess() {
            when(authentication.getName()).thenReturn("teacher1");
            doNothing().when(studentService).deleteStudent(1L, "teacher1");

            ResponseEntity<ApiResponse<Void>> response =
                    studentController.deleteStudent(1L, authentication);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Student deleted successfully", response.getBody().getMessage());
            verify(studentService, times(1)).deleteStudent(1L, "teacher1");
        }
    }
}
