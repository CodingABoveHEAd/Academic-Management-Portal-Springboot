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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

    public TeacherResponse createTeacher(TeacherRequest request) {
        // Check for duplicates
        if (teacherRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Teacher", "employeeId", request.getEmployeeId());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        Department department = departmentService.getDepartmentEntity(request.getDepartmentId());

        // Create User for authentication
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.ROLE_TEACHER);
        user.setEnabled(true);

        // Create Teacher
        Teacher teacher = new Teacher();
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setEmployeeId(request.getEmployeeId());
        teacher.setSpecialization(request.getSpecialization());
        teacher.setDepartment(department);
        teacher.setUser(user);

        Teacher savedTeacher = teacherRepository.save(teacher);
        return mapToResponse(savedTeacher);
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        return mapToResponse(teacher);
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherByUsername(String username) {
        Teacher teacher = teacherRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "username", username));
        return mapToResponse(teacher);
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getTeachersByDepartment(Long departmentId) {
        return teacherRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TeacherResponse updateTeacher(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        // Check for duplicate employee ID if it's being changed
        if (!teacher.getEmployeeId().equals(request.getEmployeeId()) &&
            teacherRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Teacher", "employeeId", request.getEmployeeId());
        }

        Department department = departmentService.getDepartmentEntity(request.getDepartmentId());

        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setEmployeeId(request.getEmployeeId());
        teacher.setSpecialization(request.getSpecialization());
        teacher.setDepartment(department);

        // Update email if provided
        if (request.getEmail() != null) {
            teacher.getUser().setEmail(request.getEmail());
        }

        Teacher updatedTeacher = teacherRepository.save(teacher);
        return mapToResponse(updatedTeacher);
    }

    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        teacherRepository.delete(teacher);
    }

    @Transactional(readOnly = true)
    public Teacher getTeacherEntity(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
    }

    @Transactional(readOnly = true)
    public Teacher getTeacherEntityByUsername(String username) {
        return teacherRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "username", username));
    }

    private TeacherResponse mapToResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .employeeId(teacher.getEmployeeId())
                .specialization(teacher.getSpecialization())
                .departmentName(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null)
                .departmentId(teacher.getDepartment() != null ? teacher.getDepartment().getId() : null)
                .email(teacher.getUser() != null ? teacher.getUser().getEmail() : null)
                .studentCount(teacher.getStudents() != null ? teacher.getStudents().size() : 0)
                .build();
    }
}
