package com.niloy.student_portal.service;

import com.niloy.student_portal.dto.request.StudentCreateRequest;
import com.niloy.student_portal.dto.request.StudentUpdateRequest;
import com.niloy.student_portal.dto.response.StudentResponse;
import com.niloy.student_portal.entity.Course;
import com.niloy.student_portal.entity.Role;
import com.niloy.student_portal.entity.Student;
import com.niloy.student_portal.entity.Teacher;
import com.niloy.student_portal.entity.User;
import com.niloy.student_portal.exception.BadRequestException;
import com.niloy.student_portal.exception.DuplicateResourceException;
import com.niloy.student_portal.exception.ResourceNotFoundException;
import com.niloy.student_portal.exception.UnauthorizedAccessException;
import com.niloy.student_portal.repository.StudentRepository;
import com.niloy.student_portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final TeacherService teacherService;
    private final CourseService courseService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new student - Only teachers can create students
     */
    public StudentResponse createStudent(StudentCreateRequest request, String teacherUsername) {
        // Check for duplicates
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new DuplicateResourceException("Student", "studentId", request.getStudentId());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Get the teacher who is creating this student
        Teacher teacher = teacherService.getTeacherEntityByUsername(teacherUsername);

        // Create User for authentication
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.ROLE_STUDENT);
        user.setEnabled(true);

        // Create Student
        Student student = new Student();
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setStudentId(request.getStudentId());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAddress(request.getAddress());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setUser(user);
        student.setTeacher(teacher);

        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentByUsername(String username) {
        Student student = studentRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", username));
        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByTeacher(String teacherUsername) {
        Teacher teacher = teacherService.getTeacherEntityByUsername(teacherUsername);
        return studentRepository.findByTeacher(teacher).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByCourse(Long courseId) {
        return studentRepository.findByEnrolledCourseId(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update student by teacher - full update
     */
    public StudentResponse updateStudentByTeacher(Long id, StudentCreateRequest request, String teacherUsername) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        Teacher teacher = teacherService.getTeacherEntityByUsername(teacherUsername);

        // Verify the teacher manages this student
        if (!student.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to update this student");
        }

        // Check for duplicate student ID if it's being changed
        if (!student.getStudentId().equals(request.getStudentId()) &&
            studentRepository.existsByStudentId(request.getStudentId())) {
            throw new DuplicateResourceException("Student", "studentId", request.getStudentId());
        }

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setStudentId(request.getStudentId());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAddress(request.getAddress());
        student.setPhoneNumber(request.getPhoneNumber());

        if (request.getEmail() != null) {
            student.getUser().setEmail(request.getEmail());
        }

        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    /**
     * Update student by student themselves - limited fields
     */
    public StudentResponse updateStudentProfile(StudentUpdateRequest request, String studentUsername) {
        Student student = studentRepository.findByUserUsername(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", studentUsername));

        // Students can only update limited fields
        if (request.getFirstName() != null) {
            student.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            student.setLastName(request.getLastName());
        }
        if (request.getDateOfBirth() != null) {
            student.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            student.setAddress(request.getAddress());
        }
        if (request.getPhoneNumber() != null) {
            student.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            // Check for duplicate email
            if (!student.getUser().getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
            student.getUser().setEmail(request.getEmail());
        }

        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    /**
     * Delete student - Only the teacher who manages the student can delete
     */
    public void deleteStudent(Long id, String teacherUsername) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        Teacher teacher = teacherService.getTeacherEntityByUsername(teacherUsername);

        // Verify the teacher manages this student
        if (!student.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to delete this student");
        }

        studentRepository.delete(student);
    }

    /**
     * Enroll student in a course - Student can only enroll in one course at a time
     */
    public StudentResponse enrollInCourse(Long courseId, String studentUsername) {
        Student student = studentRepository.findByUserUsername(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", studentUsername));

        // Check if student is already enrolled in a course
        if (student.getEnrolledCourse() != null) {
            throw new BadRequestException("You are already enrolled in a course: " +
                student.getEnrolledCourse().getCourseName() + ". Please drop the current course before enrolling in a new one.");
        }

        Course course = courseService.getCourseEntity(courseId);
        student.setEnrolledCourse(course);

        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    /**
     * Drop current course enrollment
     */
    public StudentResponse dropCourse(String studentUsername) {
        Student student = studentRepository.findByUserUsername(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", studentUsername));

        if (student.getEnrolledCourse() == null) {
            throw new BadRequestException("You are not enrolled in any course");
        }

        student.setEnrolledCourse(null);

        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    @Transactional(readOnly = true)
    public Student getStudentEntity(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    private StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .studentId(student.getStudentId())
                .dateOfBirth(student.getDateOfBirth())
                .address(student.getAddress())
                .phoneNumber(student.getPhoneNumber())
                .email(student.getUser() != null ? student.getUser().getEmail() : null)
                .teacherName(student.getTeacher() != null ?
                    student.getTeacher().getFirstName() + " " + student.getTeacher().getLastName() : null)
                .teacherId(student.getTeacher() != null ? student.getTeacher().getId() : null)
                .enrolledCourseName(student.getEnrolledCourse() != null ?
                    student.getEnrolledCourse().getCourseName() : null)
                .enrolledCourseId(student.getEnrolledCourse() != null ?
                    student.getEnrolledCourse().getId() : null)
                .build();
    }
}
