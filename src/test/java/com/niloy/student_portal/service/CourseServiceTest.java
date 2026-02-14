package com.niloy.student_portal.service;

import com.niloy.student_portal.dto.request.CourseRequest;
import com.niloy.student_portal.dto.response.CourseResponse;
import com.niloy.student_portal.entity.Course;
import com.niloy.student_portal.entity.Department;
import com.niloy.student_portal.exception.DuplicateResourceException;
import com.niloy.student_portal.exception.ResourceNotFoundException;
import com.niloy.student_portal.repository.CourseRepository;
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
@DisplayName("CourseService Tests")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private CourseService courseService;

    private CourseRequest courseRequest;
    private Course course;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");
        department.setDescription("CS Department");

        courseRequest = new CourseRequest();
        courseRequest.setCourseCode("CS101");
        courseRequest.setCourseName("Introduction to Computer Science");
        courseRequest.setDescription("Basic CS course");
        courseRequest.setCredits(3);
        courseRequest.setDepartmentId(1L);

        course = new Course();
        course.setId(1L);
        course.setCourseCode("CS101");
        course.setCourseName("Introduction to Computer Science");
        course.setDescription("Basic CS course");
        course.setCredits(3);
        course.setDepartment(department);
        course.setEnrolledStudents(new ArrayList<>());
    }

    @Nested
    @DisplayName("Create Course Tests")
    class CreateCourseTests {

        @Test
        @DisplayName("Should create course successfully")
        void createCourse_WithValidRequest_ShouldReturnCourseResponse() {
            // Arrange
            when(courseRepository.existsByCourseCode("CS101")).thenReturn(false);
            when(departmentService.getDepartmentEntity(1L)).thenReturn(department);
            when(courseRepository.save(any(Course.class))).thenReturn(course);

            // Act
            CourseResponse response = courseService.createCourse(courseRequest);

            // Assert
            assertNotNull(response);
            assertEquals("CS101", response.getCourseCode());
            assertEquals("Introduction to Computer Science", response.getCourseName());
            assertEquals(3, response.getCredits());
            assertEquals("Computer Science", response.getDepartmentName());
            verify(courseRepository, times(1)).existsByCourseCode("CS101");
            verify(courseRepository, times(1)).save(any(Course.class));
        }

        @Test
        @DisplayName("Should throw exception when course code already exists")
        void createCourse_WithDuplicateCode_ShouldThrowException() {
            // Arrange
            when(courseRepository.existsByCourseCode("CS101")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> courseService.createCourse(courseRequest)
            );
            assertTrue(exception.getMessage().contains("Course"));
            verify(courseRepository, times(1)).existsByCourseCode("CS101");
            verify(courseRepository, never()).save(any(Course.class));
        }
    }

    @Nested
    @DisplayName("Get Course By ID Tests")
    class GetCourseByIdTests {

        @Test
        @DisplayName("Should return course when found")
        void getCourseById_WhenExists_ShouldReturnCourseResponse() {
            // Arrange
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            // Act
            CourseResponse response = courseService.getCourseById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("CS101", response.getCourseCode());
            verify(courseRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when course not found")
        void getCourseById_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(courseRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> courseService.getCourseById(1L)
            );
            assertTrue(exception.getMessage().contains("Course"));
            verify(courseRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("Get All Courses Tests")
    class GetAllCoursesTests {

        @Test
        @DisplayName("Should return all courses")
        void getAllCourses_ShouldReturnList() {
            // Arrange
            Course course2 = new Course();
            course2.setId(2L);
            course2.setCourseCode("CS102");
            course2.setCourseName("Data Structures");
            course2.setCredits(3);
            course2.setDepartment(department);
            course2.setEnrolledStudents(new ArrayList<>());

            List<Course> courses = Arrays.asList(course, course2);
            when(courseRepository.findAll()).thenReturn(courses);

            // Act
            List<CourseResponse> response = courseService.getAllCourses();

            // Assert
            assertNotNull(response);
            assertEquals(2, response.size());
            assertEquals("CS101", response.get(0).getCourseCode());
            assertEquals("CS102", response.get(1).getCourseCode());
            verify(courseRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no courses exist")
        void getAllCourses_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(courseRepository.findAll()).thenReturn(new ArrayList<>());

            // Act
            List<CourseResponse> response = courseService.getAllCourses();

            // Assert
            assertNotNull(response);
            assertTrue(response.isEmpty());
            verify(courseRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Get Courses By Department Tests")
    class GetCoursesByDepartmentTests {

        @Test
        @DisplayName("Should return courses for department")
        void getCoursesByDepartment_ShouldReturnList() {
            // Arrange
            List<Course> courses = Arrays.asList(course);
            when(courseRepository.findByDepartmentId(1L)).thenReturn(courses);

            // Act
            List<CourseResponse> response = courseService.getCoursesByDepartment(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.size());
            assertEquals("CS101", response.get(0).getCourseCode());
            verify(courseRepository, times(1)).findByDepartmentId(1L);
        }

        @Test
        @DisplayName("Should return empty list when no courses in department")
        void getCoursesByDepartment_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(courseRepository.findByDepartmentId(1L)).thenReturn(new ArrayList<>());

            // Act
            List<CourseResponse> response = courseService.getCoursesByDepartment(1L);

            // Assert
            assertNotNull(response);
            assertTrue(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("Update Course Tests")
    class UpdateCourseTests {

        @Test
        @DisplayName("Should update course successfully")
        void updateCourse_WithValidRequest_ShouldReturnUpdatedCourseResponse() {
            // Arrange
            CourseRequest updateRequest = new CourseRequest();
            updateRequest.setCourseCode("CS101-Updated");
            updateRequest.setCourseName("Updated Course Name");
            updateRequest.setDescription("Updated description");
            updateRequest.setCredits(4);
            updateRequest.setDepartmentId(1L);

            Course updatedCourse = new Course();
            updatedCourse.setId(1L);
            updatedCourse.setCourseCode("CS101-Updated");
            updatedCourse.setCourseName("Updated Course Name");
            updatedCourse.setDescription("Updated description");
            updatedCourse.setCredits(4);
            updatedCourse.setDepartment(department);
            updatedCourse.setEnrolledStudents(new ArrayList<>());

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(courseRepository.existsByCourseCode("CS101-Updated")).thenReturn(false);
            when(departmentService.getDepartmentEntity(1L)).thenReturn(department);
            when(courseRepository.save(any(Course.class))).thenReturn(updatedCourse);

            // Act
            CourseResponse response = courseService.updateCourse(1L, updateRequest);

            // Assert
            assertNotNull(response);
            assertEquals("CS101-Updated", response.getCourseCode());
            assertEquals("Updated Course Name", response.getCourseName());
            assertEquals(4, response.getCredits());
            verify(courseRepository, times(1)).findById(1L);
            verify(courseRepository, times(1)).save(any(Course.class));
        }

        @Test
        @DisplayName("Should update course when code is unchanged")
        void updateCourse_WithSameCode_ShouldNotCheckDuplicate() {
            // Arrange
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(departmentService.getDepartmentEntity(1L)).thenReturn(department);
            when(courseRepository.save(any(Course.class))).thenReturn(course);

            // Act
            CourseResponse response = courseService.updateCourse(1L, courseRequest);

            // Assert
            assertNotNull(response);
            verify(courseRepository, never()).existsByCourseCode(anyString());
        }

        @Test
        @DisplayName("Should throw exception when new code already exists")
        void updateCourse_WithDuplicateCode_ShouldThrowException() {
            // Arrange
            CourseRequest updateRequest = new CourseRequest();
            updateRequest.setCourseCode("CS102");
            updateRequest.setCourseName("Test");
            updateRequest.setDepartmentId(1L);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(courseRepository.existsByCourseCode("CS102")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> courseService.updateCourse(1L, updateRequest)
            );
            assertTrue(exception.getMessage().contains("Course"));
            verify(courseRepository, never()).save(any(Course.class));
        }

        @Test
        @DisplayName("Should throw exception when course not found")
        void updateCourse_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(courseRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> courseService.updateCourse(1L, courseRequest)
            );
            assertTrue(exception.getMessage().contains("Course"));
        }
    }

    @Nested
    @DisplayName("Delete Course Tests")
    class DeleteCourseTests {

        @Test
        @DisplayName("Should delete course successfully")
        void deleteCourse_WhenExists_ShouldDeleteSuccessfully() {
            // Arrange
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            doNothing().when(courseRepository).delete(course);

            // Act
            courseService.deleteCourse(1L);

            // Assert
            verify(courseRepository, times(1)).findById(1L);
            verify(courseRepository, times(1)).delete(course);
        }

        @Test
        @DisplayName("Should throw exception when course not found")
        void deleteCourse_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(courseRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> courseService.deleteCourse(1L)
            );
            assertTrue(exception.getMessage().contains("Course"));
            verify(courseRepository, never()).delete(any(Course.class));
        }
    }

    @Nested
    @DisplayName("Get Course Entity Tests")
    class GetCourseEntityTests {

        @Test
        @DisplayName("Should return course entity when found")
        void getCourseEntity_WhenExists_ShouldReturnCourse() {
            // Arrange
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            // Act
            Course result = courseService.getCourseEntity(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("CS101", result.getCourseCode());
            verify(courseRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when course entity not found")
        void getCourseEntity_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(courseRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> courseService.getCourseEntity(1L)
            );
            assertTrue(exception.getMessage().contains("Course"));
        }
    }
}

