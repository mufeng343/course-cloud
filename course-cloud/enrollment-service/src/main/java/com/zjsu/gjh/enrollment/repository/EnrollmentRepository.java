package com.zjsu.gjh.enrollment.repository;

import com.zjsu.gjh.enrollment.model.Enrollment;
import com.zjsu.gjh.enrollment.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByCourseId(String courseId);

    List<Enrollment> findByStudentId(String studentId);

    List<Enrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status);

    List<Enrollment> findByStudentIdAndStatus(String studentId, EnrollmentStatus status);

    Optional<Enrollment> findByCourseIdAndStudentId(String courseId, String studentId);

    boolean existsByCourseIdAndStudentId(String courseId, String studentId);

    boolean existsByCourseIdAndStudentIdAndStatus(String courseId, String studentId, EnrollmentStatus status);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId AND e.status = 'ACTIVE'")
    int countActiveEnrollmentsByCourseId(@Param("courseId") String courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.studentId = :studentId AND e.status = 'ACTIVE'")
    int countActiveEnrollmentsByStudentId(@Param("studentId") String studentId);
}