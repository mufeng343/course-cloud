package com.zjsu.gjh.enrollment.service;

import com.zjsu.gjh.enrollment.exception.ResourceNotFoundException;
import com.zjsu.gjh.enrollment.model.Student;
import com.zjsu.gjh.enrollment.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class StudentService {
    private final StudentRepository studentRepository;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentsById(String id) {
        return studentRepository.findById(id);
    }

    public Student createStudent(Student student) {
        // 验证必填字段
        validateRequiredFields(student);

        // 验证邮箱格式
        validateEmail(student.getEmail());

        // 检查学号是否已存在
        if (studentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException("学号已存在: " + student.getStudentId());
        }

        // 检查邮箱是否已存在
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在: " + student.getEmail());
        }

        // 创建新学生（id和createdAt会自动生成）
        Student newStudent = new Student();
        newStudent.setStudentId(student.getStudentId());
        newStudent.setName(student.getName());
        newStudent.setMajor(student.getMajor());
        newStudent.setGrade(student.getGrade());
        newStudent.setEmail(student.getEmail());

        return studentRepository.save(newStudent);
    }

    public Student updateStudent(String id, Student student) {
        // 检查学生是否存在
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        // 验证必填字段
        validateRequiredFields(student);

        // 验证邮箱格式
        validateEmail(student.getEmail());

        // 检查学号是否被其他学生使用
        if (!existingStudent.getStudentId().equals(student.getStudentId()) &&
                studentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException("学号已被其他学生使用: " + student.getStudentId());
        }

        // 检查邮箱是否被其他学生使用
        if (!existingStudent.getEmail().equals(student.getEmail()) &&
                studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("邮箱已被其他学生使用: " + student.getEmail());
        }

        // 更新字段（保持原有的id和createdAt）
        existingStudent.setStudentId(student.getStudentId());
        existingStudent.setName(student.getName());
        existingStudent.setMajor(student.getMajor());
        existingStudent.setGrade(student.getGrade());
        existingStudent.setEmail(student.getEmail());

        return studentRepository.save(existingStudent);
    }

    // 原有的验证方法保持不变...
    private void validateRequiredFields(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (student.getMajor() == null || student.getMajor().trim().isEmpty()) {
            throw new IllegalArgumentException("专业不能为空");
        }
        if (student.getGrade() == null) {
            throw new IllegalArgumentException("入学年份不能为空");
        }
        if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确，必须包含@和域名: " + email);
        }
    }

    public void deleteStudent(String id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", id);
        }
        studentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        return studentRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<Student> getStudentsByMajor(String major) {
        return studentRepository.findByMajor(major);
    }

    @Transactional(readOnly = true)
    public List<Student> getStudentsByGrade(Integer grade) {
        return studentRepository.findByGrade(grade);
    }
}