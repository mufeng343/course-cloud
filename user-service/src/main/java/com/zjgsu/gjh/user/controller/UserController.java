package com.zjgsu.gjh.user.controller;

import com.zjgsu.gjh.user.model.Student;
import com.zjgsu.gjh.user.model.Teacher;
import com.zjgsu.gjh.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Value("${server.port}")
    private String port;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse createStudent(@Valid @RequestBody StudentRequest request) {
        Student student = new Student(
            request.username(),
            request.email(),
            request.studentId(),
            request.name(),
            request.major(),
            request.grade()
        );
        Student created = userService.createStudent(student);
        return StudentResponse.from(created);
    }

    @GetMapping("/students")
    public List<StudentResponse> getAllStudents() {
        return userService.getAllStudents().stream()
            .map(StudentResponse::from)
            .collect(Collectors.toList());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable String id) {
        log.info("User Service instance on port {} handling request for student: {}", port, id);
        return userService.getStudentById(id)
            .map(StudentResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherResponse createTeacher(@Valid @RequestBody TeacherRequest request) {
        Teacher teacher = new Teacher(
            request.username(),
            request.email(),
            request.teacherId(),
            request.name(),
            request.department(),
            request.title()
        );
        Teacher created = userService.createTeacher(teacher);
        return TeacherResponse.from(created);
    }

    @GetMapping("/teachers")
    public List<TeacherResponse> getAllTeachers() {
        return userService.getAllTeachers().stream()
            .map(TeacherResponse::from)
            .collect(Collectors.toList());
    }

    @GetMapping("/teachers/{id}")
    public ResponseEntity<TeacherResponse> getTeacherById(@PathVariable String id) {
        return userService.getTeacherById(id)
            .map(TeacherResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable String id) {
        userService.deleteStudent(id);
    }

    @DeleteMapping("/teachers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeacher(@PathVariable String id) {
        userService.deleteTeacher(id);
    }
}
