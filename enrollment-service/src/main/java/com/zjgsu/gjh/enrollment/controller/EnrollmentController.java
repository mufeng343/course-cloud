package com.zjgsu.gjh.enrollment.controller;

import com.zjgsu.gjh.enrollment.model.EnrollmentRecord;
import com.zjgsu.gjh.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentRecord record = enrollmentService.enroll(request.courseId(), request.studentId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new EnrollmentResponse(record.getId(), record.getCourseId(), record.getStudentId(), record.getEnrolledAt().toString()));
    }

    @GetMapping("/course/{courseId}")
    public List<EnrollmentResponse> listByCourse(@PathVariable String courseId) {
        return enrollmentService.listByCourse(courseId)
                .stream()
                .map(record -> new EnrollmentResponse(record.getId(), record.getCourseId(), record.getStudentId(), record.getEnrolledAt().toString()))
                .toList();
    }

    @GetMapping
    public List<EnrollmentResponse> listAll() {
        return enrollmentService.listAll()
                .stream()
                .map(record -> new EnrollmentResponse(record.getId(), record.getCourseId(), record.getStudentId(), record.getEnrolledAt().toString()))
                .toList();
    }

    public record EnrollmentRequest(@NotBlank String courseId, @NotBlank String studentId) {}

    public record EnrollmentResponse(String id, String courseId, String studentId, String enrolledAt) {}
}
