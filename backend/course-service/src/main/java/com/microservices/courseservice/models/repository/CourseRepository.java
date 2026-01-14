package com.microservices.courseservice.models.repository;

import com.microservices.courseservice.models.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.transaction.annotation.Transactional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c JOIN c.courseStudents cs WHERE cs.studentId = :studentId")
    Course findCourseByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT c FROM Course c WHERE UPPER(c.name) LIKE UPPER(CONCAT('%', :text, '%')) OR UPPER(c.description) LIKE UPPER(CONCAT('%', :text, '%'))")
    Page<Course> findByNameOrDescriptionWithPageable(@Param("text") String text, Pageable pageable);

    @Transactional
    @Modifying
    @Query("DELETE FROM CourseStudent cs WHERE cs.studentId = :studentId")
    void deleteCourseStudentById(@Param("studentId") Long studentId);
}