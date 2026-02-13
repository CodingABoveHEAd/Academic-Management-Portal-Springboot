package com.niloy.student_portal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niloy.student_portal.dto.request.StudentCreateRequest;
import com.niloy.student_portal.dto.response.StudentResponse;
import com.niloy.student_portal.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@DisplayName("StudentController Unit Tests")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private StudentCreateRequest createRequest;
    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
        // Initialize test data
        createRequest = new StudentCreateRequest();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setStudentId("STU001");
        createRequest.setDateOfBirth(LocalDate.of(2000, 1, 1));
        createRequest.setAddress("123 Main St");
        createRequest.setPhoneNumber("1234567890");
        createRequest.setUsername("johndoe");
        createRequest.setPassword("password123");
        createRequest.setEmail("john.doe@student.edu");

        studentResponse = StudentResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .studentId("STU001")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("123 Main St")
                .phoneNumber("1234567890")
                .email("john.doe@student.edu")
                .teacherName("Prof. Smith")
                .teacherId(1L)
                .build();
    }

    @Test
    @DisplayName("Create Student - Success (Teacher Role)")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testCreateStudent_Success() throws Exception {
        // Given
        when(studentService.createStudent(any(StudentCreateRequest.class), eq("teacher")))
                .thenReturn(studentResponse);

        // When & Then
        mockMvc.perform(post("/api/students")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.studentId").value("STU001"))
                .andExpect(jsonPath("$.data.email").value("john.doe@student.edu"))
                .andExpect(jsonPath("$.data.teacherName").value("Prof. Smith"));

        verify(studentService, times(1)).createStudent(any(StudentCreateRequest.class), eq("teacher"));
    }

    @Test
    @DisplayName("Create Student - Unauthorized (No Authentication)")
    void testCreateStudent_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/students")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(studentService, never()).createStudent(any(), any());
    }

    @Test
    @DisplayName("Create Student - Forbidden (Student Role)")
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testCreateStudent_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/students")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(studentService, never()).createStudent(any(), any());
    }

    @Test
    @DisplayName("Get Student By ID - Success")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testGetStudentById_Success() throws Exception {
        // Given
        when(studentService.getStudentById(1L)).thenReturn(studentResponse);

        // When & Then
        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.studentId").value("STU001"));

        verify(studentService, times(1)).getStudentById(1L);
    }

    @Test
    @DisplayName("Get Student By ID - Unauthorized")
    void testGetStudentById_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isUnauthorized());

        verify(studentService, never()).getStudentById(any());
    }

    @Test
    @DisplayName("Get All Students - Success (Teacher Role)")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testGetAllStudents_Success() throws Exception {
        // Given
        StudentResponse student2 = StudentResponse.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .studentId("STU002")
                .email("jane.smith@student.edu")
                .build();

        List<StudentResponse> students = Arrays.asList(studentResponse, student2);
        when(studentService.getAllStudents()).thenReturn(students);

        // When & Then
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].firstName").value("John"))
                .andExpect(jsonPath("$.data[1].firstName").value("Jane"));

        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("Get All Students - Forbidden (Student Role)")
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testGetAllStudents_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getAllStudents();
    }

    @Test
    @DisplayName("Get My Students - Success (Teacher Role)")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testGetMyStudents_Success() throws Exception {
        // Given
        List<StudentResponse> students = Arrays.asList(studentResponse);
        when(studentService.getStudentsByTeacher("teacher")).thenReturn(students);

        // When & Then
        mockMvc.perform(get("/api/students/my-students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].teacherName").value("Prof. Smith"));

        verify(studentService, times(1)).getStudentsByTeacher("teacher");
    }

    @Test
    @DisplayName("Get My Students - Forbidden (Student Role)")
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testGetMyStudents_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/students/my-students"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getStudentsByTeacher(any());
    }

    @Test
    @DisplayName("Get Students By Course - Success (Teacher Role)")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testGetStudentsByCourse_Success() throws Exception {
        // Given
        List<StudentResponse> students = Arrays.asList(studentResponse);
        when(studentService.getStudentsByCourse(1L)).thenReturn(students);

        // When & Then
        mockMvc.perform(get("/api/students/course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].studentId").value("STU001"));

        verify(studentService, times(1)).getStudentsByCourse(1L);
    }

    @Test
    @DisplayName("Get Students By Course - Forbidden (Student Role)")
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testGetStudentsByCourse_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/students/course/1"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getStudentsByCourse(any());
    }

    @Test
    @DisplayName("Update Student By Teacher - Success")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testUpdateStudentByTeacher_Success() throws Exception {
        // Given
        StudentResponse updatedStudent = StudentResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Updated")
                .studentId("STU001")
                .email("john.updated@student.edu")
                .build();

        when(studentService.updateStudentByTeacher(eq(1L), any(StudentCreateRequest.class), eq("teacher")))
                .thenReturn(updatedStudent);

        // When & Then
        mockMvc.perform(put("/api/students/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student updated successfully"))
                .andExpect(jsonPath("$.data.lastName").value("Updated"))
                .andExpect(jsonPath("$.data.email").value("john.updated@student.edu"));

        verify(studentService, times(1)).updateStudentByTeacher(eq(1L), any(StudentCreateRequest.class), eq("teacher"));
    }

    @Test
    @DisplayName("Update Student By Teacher - Forbidden (Student Role)")
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testUpdateStudentByTeacher_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/students/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(studentService, never()).updateStudentByTeacher(any(), any(), any());
    }

    @Test
    @DisplayName("Update Student By Teacher - Unauthorized")
    void testUpdateStudentByTeacher_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/students/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(studentService, never()).updateStudentByTeacher(any(), any(), any());
    }

    @Test
    @DisplayName("Delete Student - Success (Teacher Role)")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testDeleteStudent_Success() throws Exception {
        // Given
        doNothing().when(studentService).deleteStudent(1L, "teacher");

        // When & Then
        mockMvc.perform(delete("/api/students/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(studentService, times(1)).deleteStudent(1L, "teacher");
    }

    @Test
    @DisplayName("Delete Student - Forbidden (Student Role)")
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testDeleteStudent_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/students/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(studentService, never()).deleteStudent(any(), any());
    }

    @Test
    @DisplayName("Delete Student - Unauthorized")
    void testDeleteStudent_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/students/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(studentService, never()).deleteStudent(any(), any());
    }

    @Test
    @DisplayName("Get All Students - Empty List")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testGetAllStudents_EmptyList() throws Exception {
        // Given
        when(studentService.getAllStudents()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("Create Student - With All Fields")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testCreateStudent_WithAllFields() throws Exception {
        // Given
        StudentResponse completeResponse = StudentResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .studentId("STU001")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("123 Main St")
                .phoneNumber("1234567890")
                .email("john.doe@student.edu")
                .teacherName("Prof. Smith")
                .teacherId(1L)
                .enrolledCourseName("Computer Science 101")
                .enrolledCourseId(10L)
                .build();

        when(studentService.createStudent(any(StudentCreateRequest.class), eq("teacher")))
                .thenReturn(completeResponse);

        // When & Then
        mockMvc.perform(post("/api/students")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enrolledCourseName").value("Computer Science 101"))
                .andExpect(jsonPath("$.data.enrolledCourseId").value(10));

        verify(studentService, times(1)).createStudent(any(StudentCreateRequest.class), eq("teacher"));
    }

    @Test
    @DisplayName("Get Students By Course - With Multiple Students")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testGetStudentsByCourse_MultipleStudents() throws Exception {
        // Given
        StudentResponse student2 = StudentResponse.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .studentId("STU002")
                .build();

        List<StudentResponse> students = Arrays.asList(studentResponse, student2);
        when(studentService.getStudentsByCourse(1L)).thenReturn(students);

        // When & Then
        mockMvc.perform(get("/api/students/course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].studentId").value("STU001"))
                .andExpect(jsonPath("$.data[1].studentId").value("STU002"));

        verify(studentService, times(1)).getStudentsByCourse(1L);
    }
}
