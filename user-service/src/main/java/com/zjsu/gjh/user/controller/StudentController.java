package com.zjsu.gjh.user.controller;

import com.zjsu.gjh.user.common.ApiResponse;
import com.zjsu.gjh.user.model.Student;
import com.zjsu.gjh.user.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final String serverPort;

    public StudentController(StudentService studentService,
                             @org.springframework.beans.factory.annotation.Value("${server.port}") String serverPort) {
        this.studentService = studentService;
        this.serverPort = serverPort;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Student>>> all() {
        return ResponseEntity.ok(ApiResponse.success(studentService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> byId(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(studentService.findById(id)));
    }

    @GetMapping("/studentId/{studentId}")
    public ResponseEntity<ApiResponse<Student>> byStudentId(@PathVariable String studentId) {
        return ResponseEntity.ok(ApiResponse.success(studentService.findByStudentId(studentId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Student>> create(@Valid @RequestBody Student student) {
        Student created = studentService.create(student);
        return ResponseEntity.status(201).body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> update(@PathVariable String id, @Valid @RequestBody Student student) {
        Student updated = studentService.update(id, student);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("port", serverPort, "service", "user-service")));
    }
}
