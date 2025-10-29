package com.aminah.elearning.repository;

import com.aminah.elearning.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // ✅ Search courses by name (case-insensitive)
    List<Course> findByCourseNameContainingIgnoreCase(String courseNamePart);

    // ✅ (optional) Find all courses a user is enrolled in
    List<Course> findCoursesByEnrollments_User_Id(Long userId);

    @Query("SELECT ce.course FROM CourseEnrollment ce WHERE ce.user.id = :userId")
    List<Course> findCoursesByUserId(@Param("userId") Long userId);

}

