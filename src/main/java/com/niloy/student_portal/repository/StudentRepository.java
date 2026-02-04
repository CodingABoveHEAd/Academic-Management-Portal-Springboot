package com.niloy.student_portal.repository;

import com.niloy.student_portal.entity.Student;
import com.niloy.student_portal.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByUserUsername(String username);
    List<Student> findByTeacher(Teacher teacher);
    List<Student> findByEnrolledCourseId(Long courseId);
    boolean existsByStudentId(String studentId);
}
