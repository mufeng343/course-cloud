package com.zjsu.gjh.enrollment.controller;

import com.zjsu.gjh.enrollment.common.ApiResponse;
import com.zjsu.gjh.enrollment.model.Enrollment;
import com.zjsu.gjh.enrollment.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final String serverPort;

    public EnrollmentController(EnrollmentService enrollmentService,
                                @org.springframework.beans.factory.annotation.Value("${server.port}") String serverPort) {
        this.enrollmentService = enrollmentService;
        this.serverPort = serverPort;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Enrollment>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.getAll()));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getByCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.getByCourse(courseId)));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.getByStudent(studentId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Enrollment>> enroll(@RequestBody Map<String, String> request) {
        String courseId = request.get("courseId");
        String studentId = request.get("studentId");
        Enrollment enrollment = enrollmentService.enroll(courseId, studentId);
        return ResponseEntity.status(201).body(ApiResponse.created(enrollment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> drop(@PathVariable String id) {
        enrollmentService.drop(id);
        return ResponseEntity.noContent().build();
    }

    /** 负载均衡验证 */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<Map<String, String>>> test() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("port", serverPort, "service", "enrollment-service")));
    }
}


