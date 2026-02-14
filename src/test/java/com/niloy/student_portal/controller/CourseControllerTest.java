package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.request.CourseRequest;
import com.niloy.student_portal.dto.response.ApiResponse;
import com.niloy.student_portal.dto.response.CourseResponse;
import com.niloy.student_portal.service.CourseService;
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
@DisplayName("CourseController Tests")
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    private CourseRequest courseRequest;
    private CourseResponse courseResponse;

    @BeforeEach
    void setUp() {
        courseRequest = new CourseRequest();
        courseRequest.setCourseCode("CS101");
        courseRequest.setCourseName("Introduction to Computer Science");
        courseRequest.setDescription("Basic CS course");
        courseRequest.setCredits(3);
        courseRequest.setDepartmentId(1L);

        courseResponse = CourseResponse.builder()
                .id(1L)
                .courseCode("CS101")
                .courseName("Introduction to Computer Science")
                .description("Basic CS course")
                .credits(3)
                .departmentName("Computer Science")
                .departmentId(1L)
                .enrolledStudentCount(0)
                .build();
    }

    @Nested
    @DisplayName("Create Course Tests")
    class CreateCourseTests {

        @Test
        @DisplayName("Should create course successfully")
        void createCourse_WithValidRequest_ShouldReturnCreated() {
            // Arrange
            when(courseService.createCourse(any(CourseRequest.class))).thenReturn(courseResponse);

            // Act
            ResponseEntity<ApiResponse<CourseResponse>> response = courseController.createCourse(courseRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Course created successfully", response.getBody().getMessage());
            assertEquals("CS101", response.getBody().getData().getCourseCode());
            verify(courseService, times(1)).createCourse(any(CourseRequest.class));
        }
    }

    @Nested
    @DisplayName("Get Course By ID Tests")
    class GetCourseByIdTests {

        @Test
        @DisplayName("Should return course when found")
        void getCourseById_WhenExists_ShouldReturnCourse() {
            // Arrange
            when(courseService.getCourseById(1L)).thenReturn(courseResponse);

            // Act
            ResponseEntity<ApiResponse<CourseResponse>> response = courseController.getCourseById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("CS101", response.getBody().getData().getCourseCode());
            verify(courseService, times(1)).getCourseById(1L);
        }
    }

    @Nested
    @DisplayName("Get All Courses Tests")
    class GetAllCoursesTests {

        @Test
        @DisplayName("Should return all courses")
        void getAllCourses_ShouldReturnList() {
            // Arrange
            CourseResponse course2 = CourseResponse.builder()
                    .id(2L)
                    .courseCode("CS102")
                    .courseName("Data Structures")
                    .build();
            List<CourseResponse> courses = Arrays.asList(courseResponse, course2);
            when(courseService.getAllCourses()).thenReturn(courses);

            // Act
            ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(2, response.getBody().getData().size());
            verify(courseService, times(1)).getAllCourses();
        }

        @Test
        @DisplayName("Should return empty list when no courses exist")
        void getAllCourses_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(courseService.getAllCourses()).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().getData().isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Courses By Department Tests")
    class GetCoursesByDepartmentTests {

        @Test
        @DisplayName("Should return courses for department")
        void getCoursesByDepartment_ShouldReturnList() {
            // Arrange
            List<CourseResponse> courses = Arrays.asList(courseResponse);
            when(courseService.getCoursesByDepartment(1L)).thenReturn(courses);

            // Act
            ResponseEntity<ApiResponse<List<CourseResponse>>> response =
                    courseController.getCoursesByDepartment(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().size());
            verify(courseService, times(1)).getCoursesByDepartment(1L);
        }
    }

    @Nested
    @DisplayName("Update Course Tests")
    class UpdateCourseTests {

        @Test
        @DisplayName("Should update course successfully")
        void updateCourse_WithValidRequest_ShouldReturnUpdated() {
            // Arrange
            courseRequest.setCourseName("Updated Course Name");
            courseResponse.setCourseName("Updated Course Name");
            when(courseService.updateCourse(eq(1L), any(CourseRequest.class))).thenReturn(courseResponse);

            // Act
            ResponseEntity<ApiResponse<CourseResponse>> response =
                    courseController.updateCourse(1L, courseRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Course updated successfully", response.getBody().getMessage());
            assertEquals("Updated Course Name", response.getBody().getData().getCourseName());
            verify(courseService, times(1)).updateCourse(eq(1L), any(CourseRequest.class));
        }
    }

    @Nested
    @DisplayName("Delete Course Tests")
    class DeleteCourseTests {

        @Test
        @DisplayName("Should delete course successfully")
        void deleteCourse_WhenExists_ShouldReturnSuccess() {
            // Arrange
            doNothing().when(courseService).deleteCourse(1L);

            // Act
            ResponseEntity<ApiResponse<Void>> response = courseController.deleteCourse(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Course deleted successfully", response.getBody().getMessage());
            verify(courseService, times(1)).deleteCourse(1L);
        }
    }
}

