package com.niloy.student_portal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCreateRequest {
    private String firstName;
    private String lastName;
    private String studentId;
    private LocalDate dateOfBirth;
    private String address;
    private String phoneNumber;
    private String username;
    private String password;
    private String email;
}
