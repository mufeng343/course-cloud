package com.zjsu.gjh.enrollment.service;

import com.zjsu.gjh.enrollment.exception.ResourceNotFoundException;
import com.zjsu.gjh.enrollment.model.Enrollment;
import com.zjsu.gjh.enrollment.model.EnrollmentStatus;
import com.zjsu.gjh.enrollment.model.Student;
import com.zjsu.gjh.enrollment.repository.EnrollmentRepository;
import com.zjsu.gjh.enrollment.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate;

    @Value("${catalog-service.url}")
    private String catalogServiceUrl;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository,
                             RestTemplate restTemplate) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Enrollment> getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollmentsByCourseId(String courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    public Enrollment enrollStudent(String courseId, String studentId) {
        // 1. 验证学生是否存在
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        // 2. 调用课程目录服务验证课程是否存在
        String url = catalogServiceUrl + "/api/courses/" + courseId;
        Map<String, Object> courseResponse;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            courseResponse = response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Course", courseId);
        } catch (Exception e) {
            throw new RuntimeException("调用课程服务失败: " + e.getMessage());
        }

        // 3. 从响应中提取课程信息
        Map<String, Object> courseData = (Map<String, Object>) courseResponse.get("data");
        Integer capacity = (Integer) courseData.get("capacity");
        Integer enrolled = (Integer) courseData.get("enrolled");

        // 4. 检查课程容量
        if (enrolled >= capacity) {
            throw new IllegalArgumentException("课程容量已满，无法选课");
        }

        // 5. 检查重复选课
        if (enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(courseId, studentId, EnrollmentStatus.ACTIVE)) {
            throw new IllegalArgumentException("学生已选过该课程");
        }

        // 6. 创建选课记录
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 7. 更新课程的已选人数（调用catalog-service）
        updateCourseEnrolledCount(courseId, enrolled + 1);

        return savedEnrollment;
    }

    private void updateCourseEnrolledCount(String courseId, int newCount) {
        String url = catalogServiceUrl + "/api/courses/" + courseId;
        Map<String, Object> updateData = Map.of("enrolled", newCount);
        try {
            restTemplate.put(url, updateData);
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("Failed to update course enrolled count: " + e.getMessage());
        }
    }

    public void cancelEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", enrollmentId));

        // 获取当前课程的已选人数
        String courseId = enrollment.getCourseId();
        String url = catalogServiceUrl + "/api/courses/" + courseId;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> courseData = (Map<String, Object>) response.getBody().get("data");
            Integer enrolled = (Integer) courseData.get("enrolled");

            // 更新选课状态为退课
            enrollment.setStatus(EnrollmentStatus.DROPPED);
            enrollmentRepository.save(enrollment);

            // 更新课程的已选人数
            updateCourseEnrolledCount(courseId, enrolled - 1);

        } catch (Exception e) {
            // 即使更新课程人数失败，也完成退课操作
            enrollment.setStatus(EnrollmentStatus.DROPPED);
            enrollmentRepository.save(enrollment);
            System.err.println("Failed to update course enrolled count during cancellation: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public boolean isStudentEnrolled(String courseId, String studentId) {
        return enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(courseId, studentId, EnrollmentStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public int getEnrollmentCountByCourse(String courseId) {
        return enrollmentRepository.countActiveEnrollmentsByCourseId(courseId);
    }
}