package com.niloy.student_portal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRequest {
    private String firstName;
    private String lastName;
    private String employeeId;
    private String specialization;
    private Long departmentId;
    private String username;
    private String password;
    private String email;
}
