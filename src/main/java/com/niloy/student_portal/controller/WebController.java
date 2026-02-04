package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.response.CourseResponse;
import com.niloy.student_portal.dto.response.DepartmentResponse;
import com.niloy.student_portal.dto.response.StudentResponse;
import com.niloy.student_portal.dto.response.TeacherResponse;
import com.niloy.student_portal.entity.Role;
import com.niloy.student_portal.service.CourseService;
import com.niloy.student_portal.service.DepartmentService;
import com.niloy.student_portal.service.StudentService;
import com.niloy.student_portal.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/web")
@RequiredArgsConstructor
public class WebController {

    private final DepartmentService departmentService;
    private final CourseService courseService;
    private final StudentService studentService;
    private final TeacherService teacherService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", role);

        if (role.equals(Role.ROLE_TEACHER.name())) {
            TeacherResponse teacher = teacherService.getTeacherByUsername(authentication.getName());
            List<StudentResponse> myStudents = studentService.getStudentsByTeacher(authentication.getName());
            model.addAttribute("teacher", teacher);
            model.addAttribute("myStudents", myStudents);
            model.addAttribute("studentCount", myStudents.size());
        } else if (role.equals(Role.ROLE_STUDENT.name())) {
            StudentResponse student = studentService.getStudentByUsername(authentication.getName());
            model.addAttribute("student", student);
        }

        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        List<CourseResponse> courses = courseService.getAllCourses();

        model.addAttribute("departments", departments);
        model.addAttribute("courses", courses);
        model.addAttribute("departmentCount", departments.size());
        model.addAttribute("courseCount", courses.size());

        return "dashboard";
    }

    @GetMapping("/departments")
    public String departments(Model model, Authentication authentication) {
        addAuthInfo(model, authentication);
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        model.addAttribute("departments", departments);
        return "departments";
    }

    @GetMapping("/departments/{id}")
    public String departmentDetail(@PathVariable Long id, Model model, Authentication authentication) {
        addAuthInfo(model, authentication);
        DepartmentResponse department = departmentService.getDepartmentById(id);
        List<CourseResponse> courses = courseService.getCoursesByDepartment(id);
        List<TeacherResponse> teachers = teacherService.getTeachersByDepartment(id);
        model.addAttribute("department", department);
        model.addAttribute("courses", courses);
        model.addAttribute("teachers", teachers);
        return "department-detail";
    }

    @GetMapping("/courses")
    public String courses(Model model, Authentication authentication) {
        addAuthInfo(model, authentication);
        List<CourseResponse> courses = courseService.getAllCourses();
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        model.addAttribute("courses", courses);
        model.addAttribute("departments", departments);
        return "courses";
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable Long id, Model model, Authentication authentication) {
        addAuthInfo(model, authentication);
        CourseResponse course = courseService.getCourseById(id);
        List<StudentResponse> students = studentService.getStudentsByCourse(id);
        model.addAttribute("course", course);
        model.addAttribute("students", students);
        return "course-detail";
    }

    @GetMapping("/students")
    public String students(Model model, Authentication authentication) {
        addAuthInfo(model, authentication);
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        if (role.equals(Role.ROLE_TEACHER.name())) {
            List<StudentResponse> students = studentService.getStudentsByTeacher(authentication.getName());
            model.addAttribute("students", students);
            model.addAttribute("viewType", "my-students");
        } else {
            model.addAttribute("students", List.of());
            model.addAttribute("viewType", "none");
        }

        List<CourseResponse> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "students";
    }

    @GetMapping("/students/{id}")
    public String studentDetail(@PathVariable Long id, Model model, Authentication authentication) {
        addAuthInfo(model, authentication);
        StudentResponse student = studentService.getStudentById(id);
        model.addAttribute("student", student);
        return "student-detail";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        addAuthInfo(model, authentication);
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        if (role.equals(Role.ROLE_STUDENT.name())) {
            StudentResponse student = studentService.getStudentByUsername(authentication.getName());
            model.addAttribute("student", student);
            model.addAttribute("profileType", "student");
        } else if (role.equals(Role.ROLE_TEACHER.name())) {
            TeacherResponse teacher = teacherService.getTeacherByUsername(authentication.getName());
            model.addAttribute("teacher", teacher);
            model.addAttribute("profileType", "teacher");
        }

        return "profile";
    }

    @GetMapping("/enrollment")
    public String enrollment(Model model, Authentication authentication) {
        addAuthInfo(model, authentication);
        StudentResponse student = studentService.getStudentByUsername(authentication.getName());
        List<CourseResponse> courses = courseService.getAllCourses();
        model.addAttribute("student", student);
        model.addAttribute("courses", courses);
        return "enrollment";
    }

    private void addAuthInfo(Model model, Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", role);
        model.addAttribute("isTeacher", role.equals(Role.ROLE_TEACHER.name()));
        model.addAttribute("isStudent", role.equals(Role.ROLE_STUDENT.name()));
    }
}
