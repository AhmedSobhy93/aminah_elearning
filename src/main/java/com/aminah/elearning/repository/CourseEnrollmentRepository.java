package com.aminah.elearning.repository;

import com.aminah.elearning.model.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    List<CourseEnrollment> findByCourseId(Long courseId);

    Page<CourseEnrollment> findByCourseId(Long courseId, Pageable pageable);

    Page<CourseEnrollment> findByUserId(Long userId, Pageable pageable);

    boolean existsByCourseIdAndUserId(Long courseId, Long userId);

}