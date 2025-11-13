package com.aminah.elearning.repository;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    // All enrollments for a given user
    List<CourseEnrollment> findByUserId(Long userId);

    // Check if a user is already enrolled in a course
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByCourseIdAndUserId(int courseId, Long id);

    boolean existsByUserAndCourse(User user, Optional<Course> course);
    Optional<CourseEnrollment> findByUserAndCourse(User user, Course course);
    List<CourseEnrollment> findAlllByUser(User user);

    List<CourseEnrollment> findByCourse(Course course);
}
