package com.zjgsu.gjh.user.repository;

import com.zjgsu.gjh.user.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, String> {
    Optional<Teacher> findByTeacherId(String teacherId);
    boolean existsByTeacherId(String teacherId);
    boolean existsByEmail(String email);
}
