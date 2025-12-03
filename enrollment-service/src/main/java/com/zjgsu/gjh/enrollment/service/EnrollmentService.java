package com.zjgsu.gjh.enrollment.service;

import com.zjgsu.gjh.enrollment.model.EnrollmentRecord;
import com.zjgsu.gjh.enrollment.repository.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final RestTemplate restTemplate;
    private final EnrollmentRepository repository;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.catalog-service.url}")
    private String catalogServiceUrl;

    public EnrollmentService(RestTemplate restTemplate, EnrollmentRepository repository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    public EnrollmentRecord enroll(String courseId, String studentId) {
        // Check if already enrolled
        if (repository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new IllegalStateException("Student is already enrolled in this course");
        }

        // 1. Verify student exists by calling user-service
        try {
            String userUrl = userServiceUrl + "/api/users/students/" + studentId;
            restTemplate.getForObject(userUrl, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Student not found: " + studentId);
        } catch (Exception e) {
            log.error("Error verifying student: {}", e.getMessage());
            throw new RuntimeException("Error verifying student with user-service: " + e.getMessage());
        }

        // 2. Verify course exists and check capacity
        try {
            String courseUrl = catalogServiceUrl + "/api/courses/" + courseId;
            Map<String, Object> courseResponse = restTemplate.getForObject(courseUrl, Map.class);

            if (courseResponse == null) {
                throw new IllegalArgumentException("Course not found: " + courseId);
            }

            Integer capacity = (Integer) courseResponse.get("capacity");
            Integer enrolled = (Integer) courseResponse.get("enrolled");

            if (enrolled != null && capacity != null && enrolled >= capacity) {
                throw new IllegalStateException("Course capacity reached");
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Course not found: " + courseId);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying course: {}", e.getMessage());
            throw new RuntimeException("Error verifying course with catalog-service: " + e.getMessage());
        }

        // 3. Create enrollment record
        EnrollmentRecord record = new EnrollmentRecord(courseId, studentId);
        EnrollmentRecord saved = repository.save(record);

        log.info("选课成功: studentId={}, courseId={}", studentId, courseId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRecord> listByCourse(String courseId) {
        return repository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRecord> listByStudent(String studentId) {
        return repository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRecord> listAll() {
        return repository.findAll();
    }
}
