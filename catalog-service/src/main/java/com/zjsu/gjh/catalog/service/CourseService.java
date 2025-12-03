package com.zjsu.gjh.catalog.service;

import com.zjsu.gjh.catalog.exception.BusinessException;
import com.zjsu.gjh.catalog.exception.ResourceNotFoundException;
import com.zjsu.gjh.catalog.model.Course;
import com.zjsu.gjh.catalog.model.Instructor;
import com.zjsu.gjh.catalog.model.ScheduleSlot;
import com.zjsu.gjh.catalog.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course createCourse(Course course) {
        courseRepository.findByCode(course.getCode()).ifPresent(c -> {
            throw new BusinessException("课程代码已存在: " + course.getCode());
        });
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    public Course getCourseByCode(String code) {
        return courseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Course code", code));
    }

    public Course updateCourse(String id, CourseUpdateRequest request) {
        Course existing = getCourseById(id);

        if (request.getCode() != null && !request.getCode().equalsIgnoreCase(existing.getCode())) {
            courseRepository.findByCode(request.getCode()).ifPresent(c -> {
                throw new BusinessException("课程代码已存在: " + request.getCode());
            });
            existing.setCode(request.getCode());
        }
        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle());
        }
        Instructor instructor = request.getInstructor();
        if (instructor != null) {
            existing.setInstructor(instructor);
        }
        ScheduleSlot schedule = request.getSchedule();
        if (schedule != null) {
            existing.setSchedule(schedule);
        }
        if (request.getCapacity() != null) {
            existing.setCapacity(request.getCapacity());
        }
        if (request.getEnrolled() != null) {
            existing.setEnrolled(request.getEnrolled());
        }
        return courseRepository.save(existing);
    }

    public void deleteCourse(String id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", id);
        }
        courseRepository.deleteById(id);
    }
}


