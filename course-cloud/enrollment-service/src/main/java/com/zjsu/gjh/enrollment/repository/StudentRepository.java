package com.zjsu.gjh.enrollment.repository;

import com.zjsu.gjh.enrollment.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    Optional<Student> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);

    boolean existsByEmail(String email);

    List<Student> findByMajor(String major);

    List<Student> findByGrade(Integer grade);

    @Query("SELECT s FROM Student s WHERE s.major = :major AND s.grade = :grade")
    List<Student> findByMajorAndGrade(@Param("major") String major, @Param("grade") Integer grade);
}