package com.zjgsu.gjh.user.service;

import com.zjgsu.gjh.user.model.Student;
import com.zjgsu.gjh.user.model.Teacher;
import com.zjgsu.gjh.user.repository.StudentRepository;
import com.zjgsu.gjh.user.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public UserService(StudentRepository studentRepository, TeacherRepository teacherRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentById(String id) {
        return studentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Teacher> getTeacherById(String id) {
        return teacherRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public void deleteStudent(String id) {
        studentRepository.deleteById(id);
    }

    public void deleteTeacher(String id) {
        teacherRepository.deleteById(id);
    }
}
