package com.niloy.student_portal.service;

import com.niloy.student_portal.dto.request.CourseRequest;
import com.niloy.student_portal.dto.response.CourseResponse;
import com.niloy.student_portal.entity.Course;
import com.niloy.student_portal.entity.Department;
import com.niloy.student_portal.exception.DuplicateResourceException;
import com.niloy.student_portal.exception.ResourceNotFoundException;
import com.niloy.student_portal.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentService departmentService;

    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("Course", "courseCode", request.getCourseCode());
        }

        Department department = departmentService.getDepartmentEntity(request.getDepartmentId());

        Course course = new Course();
        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setCredits(request.getCredits());
        course.setDepartment(department);

        Course savedCourse = courseRepository.save(course);
        return mapToResponse(savedCourse);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return mapToResponse(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByDepartment(Long departmentId) {
        return courseRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Check for duplicate course code if code is being changed
        if (!course.getCourseCode().equals(request.getCourseCode()) &&
            courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("Course", "courseCode", request.getCourseCode());
        }

        Department department = departmentService.getDepartmentEntity(request.getDepartmentId());

        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setCredits(request.getCredits());
        course.setDepartment(department);

        Course updatedCourse = courseRepository.save(course);
        return mapToResponse(updatedCourse);
    }

    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public Course getCourseEntity(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .credits(course.getCredits())
                .departmentName(course.getDepartment() != null ? course.getDepartment().getName() : null)
                .departmentId(course.getDepartment() != null ? course.getDepartment().getId() : null)
                .enrolledStudentCount(course.getEnrolledStudents() != null ? course.getEnrolledStudents().size() : 0)
                .build();
    }
}
