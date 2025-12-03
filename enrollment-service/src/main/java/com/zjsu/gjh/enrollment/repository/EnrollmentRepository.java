package com.zjsu.gjh.enrollment.repository;

import com.zjsu.gjh.enrollment.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {
    boolean existsByCourseIdAndStudentId(String courseId, String studentId);

    List<Enrollment> findByCourseId(String courseId);

    List<Enrollment> findByStudentId(String studentId);
}


