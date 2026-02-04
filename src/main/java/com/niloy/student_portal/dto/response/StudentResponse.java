package com.niloy.student_portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String studentId;
    private LocalDate dateOfBirth;
    private String address;
    private String phoneNumber;
    private String email;
    private String teacherName;
    private Long teacherId;
    private String enrolledCourseName;
    private Long enrolledCourseId;
}
