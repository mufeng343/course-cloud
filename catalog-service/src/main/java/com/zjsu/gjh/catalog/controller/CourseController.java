package com.zjsu.gjh.catalog.controller;

import com.zjsu.gjh.catalog.common.ApiResponse;
import com.zjsu.gjh.catalog.model.Course;
import com.zjsu.gjh.catalog.service.CourseService;
import com.zjsu.gjh.catalog.service.CourseUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final String serverPort;

    public CourseController(CourseService courseService,
                            @org.springframework.beans.factory.annotation.Value("${server.port}") String serverPort) {
        this.courseService = courseService;
        this.serverPort = serverPort;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
        return ResponseEntity.ok(ApiResponse.success(courseService.getAllCourses()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Course>> getCourseByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseByCode(code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@Valid @RequestBody Course course) {
        Course created = courseService.createCourse(course);
        return ResponseEntity.status(201).body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(@PathVariable String id,
                                                            @RequestBody CourseUpdateRequest request) {
        Course updated = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    /** 负载均衡验证 */
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("port", serverPort, "service", "catalog-service")));
    }
}


