package com.niloy.student_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "teacher", "enrolledCourse"})
@EqualsAndHashCode(exclude = {"user", "teacher", "enrolledCourse"})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String studentId;

    private LocalDate dateOfBirth;

    private String address;

    private String phoneNumber;

    // One-to-One relationship with User for authentication
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Many Students are managed by one Teacher
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    // A Student can enroll in only one Course at a time
    // Using ManyToOne to enforce the single-course constraint
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolled_course_id")
    private Course enrolledCourse;
}
