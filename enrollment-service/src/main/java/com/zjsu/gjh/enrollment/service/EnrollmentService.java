package com.zjsu.gjh.enrollment.service;

import com.zjsu.gjh.enrollment.exception.BusinessException;
import com.zjsu.gjh.enrollment.exception.ResourceNotFoundException;
import com.zjsu.gjh.enrollment.model.Enrollment;
import com.zjsu.gjh.enrollment.model.EnrollmentStatus;
import com.zjsu.gjh.enrollment.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final RestTemplate restTemplate;

    @Value("${catalog.service.name:catalog-service}")
    private String catalogServiceName;

    @Value("${user.service.name:user-service}")
    private String userServiceName;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             RestTemplate restTemplate) {
        this.enrollmentRepository = enrollmentRepository;
        this.restTemplate = restTemplate;
    }

    public Enrollment enroll(String courseId, String studentId) {
        verifyStudentExists(studentId);

        Map<String, Object> courseResponse;
        try {
            courseResponse = restTemplate.getForObject(catalogBaseUrl() + "/api/courses/" + courseId, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        Map<String, Object> courseData = courseResponse != null ? (Map<String, Object>) courseResponse.get("data") : null;
        if (courseData == null) {
            throw new ResourceNotFoundException("Course", courseId);
        }
        Integer capacity = toInteger(courseData.get("capacity"));
        Integer enrolledCount = toInteger(courseData.get("enrolled"));

        if (capacity != null && enrolledCount != null && enrolledCount >= capacity) {
            throw new BusinessException("Course is full");
        }

        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new BusinessException("Already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());
        Enrollment saved = enrollmentRepository.save(enrollment);

        updateCourseEnrolledCount(courseId, enrolledCount != null ? enrolledCount + 1 : 1);
        return saved;
    }

    public void drop(String enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", enrollmentId));
        enrollmentRepository.deleteById(enrollment.getId());

        try {
            Map<String, Object> courseResponse = restTemplate.getForObject(catalogBaseUrl() + "/api/courses/" + enrollment.getCourseId(), Map.class);
            Map<String, Object> courseData = courseResponse != null ? (Map<String, Object>) courseResponse.get("data") : null;
            Integer enrolledCount = courseData != null ? toInteger(courseData.get("enrolled")) : null;
            if (enrolledCount != null && enrolledCount > 0) {
                updateCourseEnrolledCount(enrollment.getCourseId(), enrolledCount - 1);
            }
        } catch (Exception ignored) {
            // log in real scenario
        }
    }

    public List<Enrollment> getAll() {
        return enrollmentRepository.findAll();
    }

    public List<Enrollment> getByCourse(String courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    public List<Enrollment> getByStudent(String studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    private void updateCourseEnrolledCount(String courseId, int newCount) {
        String url = catalogBaseUrl() + "/api/courses/" + courseId;
        Map<String, Object> updateData = Map.of("enrolled", newCount);
        try {
            restTemplate.put(url, updateData);
        } catch (Exception ignored) {
            // log in real scenario
        }
    }

    private Integer toInteger(Object value) {
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        return null;
    }

    private String catalogBaseUrl() {
        return "http://" + catalogServiceName;
    }

    private void verifyStudentExists(String studentId) {
        try {
            restTemplate.getForObject(userBaseUrl() + "/api/students/studentId/" + studentId, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Student", studentId);
        }
    }

    private String userBaseUrl() {
        return "http://" + userServiceName;
    }
}
