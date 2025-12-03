package com.zjgsu.gjh.enrollment.repository;

import com.zjgsu.gjh.enrollment.model.EnrollmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentRecord, String> {
    List<EnrollmentRecord> findByCourseId(String courseId);
    List<EnrollmentRecord> findByStudentId(String studentId);
    Optional<EnrollmentRecord> findByCourseIdAndStudentId(String courseId, String studentId);
    boolean existsByCourseIdAndStudentId(String courseId, String studentId);
}
