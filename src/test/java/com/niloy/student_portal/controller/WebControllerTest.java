package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.response.CourseResponse;
import com.niloy.student_portal.dto.response.DepartmentResponse;
import com.niloy.student_portal.dto.response.StudentResponse;
import com.niloy.student_portal.dto.response.TeacherResponse;
import com.niloy.student_portal.entity.Role;
import com.niloy.student_portal.service.CourseService;
import com.niloy.student_portal.service.DepartmentService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebController Tests")
class WebControllerTest {

    @Mock
    private DepartmentService departmentService;

    @Mock
    private CourseService courseService;

    @Mock
    private StudentService studentService;

    @Mock
    private TeacherService teacherService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private WebController webController;

    private DepartmentResponse departmentResponse;
    private CourseResponse courseResponse;
    private StudentResponse studentResponse;
    private TeacherResponse teacherResponse;

    @BeforeEach
    void setUp() {
        departmentResponse = DepartmentResponse.builder()
                .id(1L)
                .name("Computer Science")
                .description("CS Department")
                .teacherCount(5)
                .courseCount(10)
                .build();

        courseResponse = CourseResponse.builder()
                .id(1L)
                .courseCode("CS101")
                .courseName("Introduction to CS")
                .credits(3)
                .departmentName("Computer Science")
                .departmentId(1L)
                .enrolledStudentCount(20)
                .build();

        studentResponse = StudentResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .studentId("STU001")
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .email("john@email.com")
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
                .email("jane@email.com")
                .studentCount(5)
                .build();
    }

    @Nested
    @DisplayName("Dashboard Tests")
    class DashboardTests {

        @Test
        @DisplayName("Should return dashboard view for teacher")
        void dashboard_AsTeacher_ShouldReturnDashboardView() {
            // Arrange
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_TEACHER.name()));
            when(authentication.getAuthorities()).thenReturn(authorities);
            when(authentication.getName()).thenReturn("teacher1");
            when(teacherService.getTeacherByUsername("teacher1")).thenReturn(teacherResponse);
            when(studentService.getStudentsByTeacher("teacher1")).thenReturn(Arrays.asList(studentResponse));
            when(departmentService.getAllDepartments()).thenReturn(Arrays.asList(departmentResponse));
            when(courseService.getAllCourses()).thenReturn(Arrays.asList(courseResponse));

            // Act
            String viewName = webController.dashboard(model, authentication);

            // Assert
            assertEquals("dashboard", viewName);
            verify(model).addAttribute("username", "teacher1");
            verify(model).addAttribute("role", Role.ROLE_TEACHER.name());
            verify(model).addAttribute("teacher", teacherResponse);
        }

        @Test
        @DisplayName("Should return dashboard view for student")
        void dashboard_AsStudent_ShouldReturnDashboardView() {
            // Arrange
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_STUDENT.name()));
            when(authentication.getAuthorities()).thenReturn(authorities);
            when(authentication.getName()).thenReturn("student1");
            when(studentService.getStudentByUsername("student1")).thenReturn(studentResponse);
            when(departmentService.getAllDepartments()).thenReturn(Arrays.asList(departmentResponse));
            when(courseService.getAllCourses()).thenReturn(Arrays.asList(courseResponse));

            // Act
            String viewName = webController.dashboard(model, authentication);

            // Assert
            assertEquals("dashboard", viewName);
            verify(model).addAttribute("username", "student1");
            verify(model).addAttribute("student", studentResponse);
        }
    }

    @Nested
    @DisplayName("Departments View Tests")
    class DepartmentsViewTests {

        @Test
        @DisplayName("Should return departments view")
        void departments_ShouldReturnDepartmentsView() {
            // Arrange
            setupAuthMock();
            List<DepartmentResponse> departments = Arrays.asList(departmentResponse);
            when(departmentService.getAllDepartments()).thenReturn(departments);

            // Act
            String viewName = webController.departments(model, authentication);

            // Assert
            assertEquals("departments", viewName);
            verify(model).addAttribute("departments", departments);
        }

        @Test
        @DisplayName("Should return department detail view")
        void departmentDetail_ShouldReturnDetailView() {
            // Arrange
            setupAuthMock();
            when(departmentService.getDepartmentById(1L)).thenReturn(departmentResponse);
            when(courseService.getCoursesByDepartment(1L)).thenReturn(Arrays.asList(courseResponse));
            when(teacherService.getTeachersByDepartment(1L)).thenReturn(Arrays.asList(teacherResponse));

            // Act
            String viewName = webController.departmentDetail(1L, model, authentication);

            // Assert
            assertEquals("department-detail", viewName);
            verify(model).addAttribute("department", departmentResponse);
            verify(model).addAttribute("courses", Arrays.asList(courseResponse));
            verify(model).addAttribute("teachers", Arrays.asList(teacherResponse));
        }
    }

    @Nested
    @DisplayName("Courses View Tests")
    class CoursesViewTests {

        @Test
        @DisplayName("Should return courses view")
        void courses_ShouldReturnCoursesView() {
            // Arrange
            setupAuthMock();
            List<CourseResponse> courses = Arrays.asList(courseResponse);
            List<DepartmentResponse> departments = Arrays.asList(departmentResponse);
            when(courseService.getAllCourses()).thenReturn(courses);
            when(departmentService.getAllDepartments()).thenReturn(departments);

            // Act
            String viewName = webController.courses(model, authentication);

            // Assert
            assertEquals("courses", viewName);
            verify(model).addAttribute("courses", courses);
            verify(model).addAttribute("departments", departments);
        }

        @Test
        @DisplayName("Should return course detail view")
        void courseDetail_ShouldReturnDetailView() {
            // Arrange
            setupAuthMock();
            when(courseService.getCourseById(1L)).thenReturn(courseResponse);
            when(studentService.getStudentsByCourse(1L)).thenReturn(Arrays.asList(studentResponse));

            // Act
            String viewName = webController.courseDetail(1L, model, authentication);

            // Assert
            assertEquals("course-detail", viewName);
            verify(model).addAttribute("course", courseResponse);
            verify(model).addAttribute("students", Arrays.asList(studentResponse));
        }
    }

    @Nested
    @DisplayName("Students View Tests")
    class StudentsViewTests {

        @Test
        @DisplayName("Should return students view for teacher")
        void students_AsTeacher_ShouldReturnStudentsView() {
            // Arrange
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_TEACHER.name()));
            when(authentication.getAuthorities()).thenReturn(authorities);
            when(authentication.getName()).thenReturn("teacher1");
            List<StudentResponse> students = Arrays.asList(studentResponse);
            when(studentService.getStudentsByTeacher("teacher1")).thenReturn(students);
            when(courseService.getAllCourses()).thenReturn(Arrays.asList(courseResponse));

            // Act
            String viewName = webController.students(model, authentication);

            // Assert
            assertEquals("students", viewName);
            verify(model).addAttribute("students", students);
            verify(model).addAttribute("viewType", "my-students");
        }

        @Test
        @DisplayName("Should return student detail view")
        void studentDetail_ShouldReturnDetailView() {
            // Arrange
            setupAuthMock();
            when(studentService.getStudentById(1L)).thenReturn(studentResponse);

            // Act
            String viewName = webController.studentDetail(1L, model, authentication);

            // Assert
            assertEquals("student-detail", viewName);
            verify(model).addAttribute("student", studentResponse);
        }
    }

    @Nested
    @DisplayName("Profile View Tests")
    class ProfileViewTests {

        @Test
        @DisplayName("Should return profile view for teacher")
        void profile_AsTeacher_ShouldReturnProfileView() {
            // Arrange
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_TEACHER.name()));
            when(authentication.getAuthorities()).thenReturn(authorities);
            when(authentication.getName()).thenReturn("teacher1");
            when(teacherService.getTeacherByUsername("teacher1")).thenReturn(teacherResponse);

            // Act
            String viewName = webController.profile(model, authentication);

            // Assert
            assertEquals("profile", viewName);
            verify(model).addAttribute("teacher", teacherResponse);
            verify(model).addAttribute("profileType", "teacher");
        }

        @Test
        @DisplayName("Should return profile view for student")
        void profile_AsStudent_ShouldReturnProfileView() {
            // Arrange
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_STUDENT.name()));
            when(authentication.getAuthorities()).thenReturn(authorities);
            when(authentication.getName()).thenReturn("student1");
            when(studentService.getStudentByUsername("student1")).thenReturn(studentResponse);

            // Act
            String viewName = webController.profile(model, authentication);

            // Assert
            assertEquals("profile", viewName);
            verify(model).addAttribute("student", studentResponse);
            verify(model).addAttribute("profileType", "student");
        }
    }

    @Nested
    @DisplayName("Enrollment View Tests")
    class EnrollmentViewTests {

        @Test
        @DisplayName("Should return enrollment view")
        void enrollment_ShouldReturnEnrollmentView() {
            // Arrange
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_STUDENT.name()));
            when(authentication.getAuthorities()).thenReturn(authorities);
            when(authentication.getName()).thenReturn("student1");
            when(studentService.getStudentByUsername("student1")).thenReturn(studentResponse);
            when(courseService.getAllCourses()).thenReturn(Arrays.asList(courseResponse));

            // Act
            String viewName = webController.enrollment(model, authentication);

            // Assert
            assertEquals("enrollment", viewName);
            verify(model).addAttribute("student", studentResponse);
            verify(model).addAttribute("courses", Arrays.asList(courseResponse));
        }
    }

    private void setupAuthMock() {
        Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_TEACHER.name()));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authentication.getName()).thenReturn("teacher1");
    }
}

