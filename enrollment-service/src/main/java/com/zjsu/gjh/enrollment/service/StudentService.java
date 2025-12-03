package com.zjsu.gjh.enrollment.service;

import com.zjsu.gjh.enrollment.exception.BusinessException;
import com.zjsu.gjh.enrollment.exception.ResourceNotFoundException;
import com.zjsu.gjh.enrollment.model.Student;
import com.zjsu.gjh.enrollment.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate;

    @Value("${user.service.name:user-service}")
    private String userServiceName;

    public StudentService(StudentRepository studentRepository, RestTemplate restTemplate) {
        this.studentRepository = studentRepository;
        this.restTemplate = restTemplate;
    }

    public Student createStudent(Student student) {
        studentRepository.findByStudentId(student.getStudentId()).ifPresent(s -> {
            throw new BusinessException("学号已存在: " + student.getStudentId());
        });
        Student saved = studentRepository.save(student);
        try {
            restTemplate.postForObject(userBaseUrl() + "/api/students", student, Object.class);
        } catch (Exception ignored) {
            // log in real scenario
        }
        return saved;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    public Student updateStudent(String id, Student update) {
        Student existing = getStudentById(id);
        existing.setName(update.getName());
        existing.setMajor(update.getMajor());
        existing.setGrade(update.getGrade());
        existing.setEmail(update.getEmail());
        Student saved = studentRepository.save(existing);
        try {
            restTemplate.put(userBaseUrl() + "/api/students/" + existing.getId(), existing);
        } catch (Exception ignored) {
            // log in real scenario
        }
        return saved;
    }

    public void deleteStudent(String id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", id);
        }
        studentRepository.deleteById(id);
        try {
            restTemplate.delete(userBaseUrl() + "/api/students/" + id);
        } catch (Exception ignored) {
            // log in real scenario
        }
    }

    private String userBaseUrl() {
        return "http://" + userServiceName;
    }
}
