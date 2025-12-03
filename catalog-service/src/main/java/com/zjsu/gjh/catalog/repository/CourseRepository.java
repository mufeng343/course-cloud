package com.zjsu.gjh.catalog.repository;

import com.zjsu.gjh.catalog.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, String> {
    Optional<Course> findByCode(String code);
}


