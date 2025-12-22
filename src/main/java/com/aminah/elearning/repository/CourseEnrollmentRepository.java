package com.aminah.elearning.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    List<CourseEnrollment> findByCourseId(Long courseId);
    Optional<CourseEnrollment> findByUserAndCourseId(User user,Long courseId);
    Page<CourseEnrollment> findByCourseId(Long courseId, Pageable pageable);

    Page<CourseEnrollment> findByUserId(Long userId, Pageable pageable);

    boolean existsByCourseIdAndUserId(Long courseId, Long userId);

    int countByCourseId(Long courseId);

    @Query("""
        SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
        FROM CourseEnrollment e
        WHERE e.user.id = :userId 
          AND e.course.id = :courseId
          AND e.paymentStatus = 'SUCCESS'
    """)
    Optional<Boolean> hasAccess(Long userId, Long courseId);

    Optional<CourseEnrollment> findByUserAndCourse(User user, Course course);

    Optional<CourseEnrollment> findByUserIdAndCourseId(Long userId, Long courseId);
}