package com.zjsu.gjh.catalog.repository;

import com.zjsu.gjh.catalog.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);

    List<Course> findByInstructorId(String instructorId);

    @Query("SELECT c FROM Course c WHERE c.title LIKE %:keyword%")
    List<Course> findByTitleContaining(@Param("keyword") String keyword);

    @Query("SELECT c FROM Course c WHERE c.capacity > c.enrolled")
    List<Course> findAvailableCourses();

    long countByInstructorId(String instructorId);
}