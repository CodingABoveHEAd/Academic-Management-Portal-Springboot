package com.niloy.student_portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String employeeId;
    private String specialization;
    private String departmentName;
    private Long departmentId;
    private String email;
    private int studentCount;
}
