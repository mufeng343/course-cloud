package com.zjsu.gjh.catalog.controller;

import com.zjsu.gjh.catalog.model.Course;
import com.zjsu.gjh.catalog.common.ApiResponse;
import com.zjsu.gjh.catalog.repository.CourseRepository;
import com.zjsu.gjh.catalog.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/courses")
public class CourseController {
    private final CourseService courseService;
    //private final CourseRepository courseRepository;

    public CourseController(CourseService courseService,CourseRepository courseRepository) {
        this.courseService = courseService;
        //this.courseRepository=courseRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses(){
        List<Course> courses=courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<Course>> getCourseByCode(@PathVariable String code){
        Optional<Course> course = courseService.getCourseByCode(code);
        return course.map(value -> ResponseEntity.ok(ApiResponse.success(value)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("Course not found with code: " + code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody Course course) {
        Course created = courseService.createCourse(course);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created));
    }

    @PutMapping("/{code}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(
            @PathVariable String code,
            @RequestBody Course course) {
        Course updated = courseService.updateCourse(code,course);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable String code) {
        courseService.deleteCourse(code);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully"));
    }
}
