package com.zjsu.gjh.user.service;

import com.zjsu.gjh.user.exception.BusinessException;
import com.zjsu.gjh.user.exception.ResourceNotFoundException;
import com.zjsu.gjh.user.model.Student;
import com.zjsu.gjh.user.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student create(Student student) {
        studentRepository.findByStudentId(student.getStudentId()).ifPresent(s -> {
            throw new BusinessException("学号已存在: " + student.getStudentId());
        });
        return studentRepository.save(student);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Student findById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    public Student findByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
    }

    public Student update(String id, Student update) {
        Student existing = findById(id);
        existing.setName(update.getName());
        existing.setMajor(update.getMajor());
        existing.setGrade(update.getGrade());
        existing.setEmail(update.getEmail());
        return studentRepository.save(existing);
    }

    public void delete(String id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", id);
        }
        studentRepository.deleteById(id);
    }
}
