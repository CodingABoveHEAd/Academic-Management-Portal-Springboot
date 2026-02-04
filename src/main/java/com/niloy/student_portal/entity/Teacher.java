package com.niloy.student_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "department", "students"})
@EqualsAndHashCode(exclude = {"user", "department", "students"})
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String employeeId;

    private String specialization;

    // One-to-One relationship with User for authentication
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Many Teachers belong to one Department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // One Teacher manages many Students
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();
}
