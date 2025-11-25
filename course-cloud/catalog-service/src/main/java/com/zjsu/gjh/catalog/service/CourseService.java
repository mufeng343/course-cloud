package com.zjsu.gjh.catalog.service;

import com.zjsu.gjh.catalog.exception.ResourceNotFoundException;
import com.zjsu.gjh.catalog.model.Course;
import com.zjsu.gjh.catalog.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Course> getCourseByCode(String code) {
        return courseRepository.findByCode(code);
    }

    public Course createCourse(Course course) {
        if (courseRepository.existsByCode(course.getCode())) {
            throw new IllegalArgumentException("课程代码已存在: " + course.getCode());
        }
        return courseRepository.save(course);
    }

    public Course updateCourse(String code, Course course) {
        if (!courseRepository.existsByCode(code)) {
            throw new ResourceNotFoundException("Course", code);
        }
        course.setCode(code);
        return courseRepository.save(course);
    }

    public void deleteCourse(String code) {
        if (!courseRepository.existsByCode(code)) {
            throw new ResourceNotFoundException("Course", code);
        }
        courseRepository.deleteById(code);
    }

    @Transactional(readOnly = true)
    public List<Course> getAvailableCourses() {
        return courseRepository.findAvailableCourses();
    }

    @Transactional(readOnly = true)
    public List<Course> searchCoursesByTitle(String keyword) {
        return courseRepository.findByTitleContaining(keyword);
    }
}