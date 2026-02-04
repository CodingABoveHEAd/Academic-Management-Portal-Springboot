package com.niloy.student_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"department", "enrolledStudents"})
@EqualsAndHashCode(exclude = {"department", "enrolledStudents"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String courseCode;

    @Column(nullable = false)
    private String courseName;

    @Column(length = 1000)
    private String description;

    private Integer credits;

    // Many Courses belong to one Department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // Many Students can enroll in this Course
    // Using OneToMany since a student can only be in one course at a time
    @OneToMany(mappedBy = "enrolledCourse")
    private List<Student> enrolledStudents = new ArrayList<>();
}
