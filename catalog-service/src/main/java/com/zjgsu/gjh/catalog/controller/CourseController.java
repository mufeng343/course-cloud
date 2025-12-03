package com.zjgsu.gjh.catalog.controller;

import com.zjgsu.gjh.catalog.model.Course;
import com.zjgsu.gjh.catalog.model.Instructor;
import com.zjgsu.gjh.catalog.model.ScheduleSlot;
import com.zjgsu.gjh.catalog.repository.CourseRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private static final Logger log = LoggerFactory.getLogger(CourseController.class);

    private final CourseRepository repository;

    @Value("${server.port}")
    private String port;

    public CourseController(CourseRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<CourseResponse> listCourses() {
        return repository.findAll()
                .stream()
                .map(CourseResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable String id) {
        log.info("Catalog Service instance on port {} handling request for course: {}", port, id);
        return repository.findById(id)
                .map(CourseResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        Course course = new Course(
                request.code(),
                request.title(),
                new Instructor(request.instructorId(), request.instructorName(), request.instructorEmail()),
                new ScheduleSlot(
                        request.dayOfWeek().toDayOfWeek(),
                        LocalTime.parse(request.start()),
                        LocalTime.parse(request.end()),
                        request.expectedAttendance()
                ),
                request.capacity()
        );
        Course saved = repository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseResponse.from(saved));
    }
}
