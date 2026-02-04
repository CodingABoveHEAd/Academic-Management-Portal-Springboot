package com.niloy.student_portal.service;

import com.niloy.student_portal.dto.request.DepartmentRequest;
import com.niloy.student_portal.dto.response.DepartmentResponse;
import com.niloy.student_portal.entity.Department;
import com.niloy.student_portal.exception.DuplicateResourceException;
import com.niloy.student_portal.exception.ResourceNotFoundException;
import com.niloy.student_portal.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }

        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());

        Department savedDepartment = departmentRepository.save(department);
        return mapToResponse(savedDepartment);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return mapToResponse(department);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Check for duplicate name if name is being changed
        if (!department.getName().equals(request.getName()) &&
            departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        Department updatedDepartment = departmentRepository.save(department);
        return mapToResponse(updatedDepartment);
    }

    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        departmentRepository.delete(department);
    }

    @Transactional(readOnly = true)
    public Department getDepartmentEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .teacherCount(department.getTeachers() != null ? department.getTeachers().size() : 0)
                .courseCount(department.getCourses() != null ? department.getCourses().size() : 0)
                .build();
    }
}
