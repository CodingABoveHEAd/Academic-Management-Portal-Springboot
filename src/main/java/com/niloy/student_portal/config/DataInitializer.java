package com.niloy.student_portal.config;

import com.niloy.student_portal.entity.*;
import com.niloy.student_portal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Only initialize if no teachers exist
        if (teacherRepository.count() == 0) {
            log.info("Initializing default data...");

            // Create a default department
            Department csDepartment = new Department();
            csDepartment.setName("Computer Science");
            csDepartment.setDescription("Department of Computer Science and Engineering");
            csDepartment = departmentRepository.save(csDepartment);

            // Create a default teacher user
            User teacherUser = new User();
            teacherUser.setUsername("teacher");
            teacherUser.setPassword(passwordEncoder.encode("teacher123"));
            teacherUser.setEmail("teacher@university.edu");
            teacherUser.setRole(Role.ROLE_TEACHER);
            teacherUser.setEnabled(true);

            // Create default teacher
            Teacher defaultTeacher = new Teacher();
            defaultTeacher.setFirstName("John");
            defaultTeacher.setLastName("Smith");
            defaultTeacher.setEmployeeId("EMP001");
            defaultTeacher.setSpecialization("Software Engineering");
            defaultTeacher.setDepartment(csDepartment);
            defaultTeacher.setUser(teacherUser);

            teacherRepository.save(defaultTeacher);

            log.info("Default data initialized successfully!");
            log.info("Default Teacher Login - Username: teacher, Password: teacher123");
        }
    }
}
