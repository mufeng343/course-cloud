package com.zjsu.gjh.enrollment.repository;

import com.zjsu.gjh.enrollment.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {
    Optional<Student> findByStudentId(String studentId);
}


