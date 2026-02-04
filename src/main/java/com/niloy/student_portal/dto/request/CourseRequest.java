package com.niloy.student_portal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {
    private String courseCode;
    private String courseName;
    private String description;
    private Integer credits;
    private Long departmentId;
}
