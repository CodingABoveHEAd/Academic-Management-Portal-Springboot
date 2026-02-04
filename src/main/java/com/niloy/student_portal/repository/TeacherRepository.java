package com.niloy.student_portal.repository;

import com.niloy.student_portal.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmployeeId(String employeeId);
    Optional<Teacher> findByUserUsername(String username);
    List<Teacher> findByDepartmentId(Long departmentId);
    boolean existsByEmployeeId(String employeeId);
}
