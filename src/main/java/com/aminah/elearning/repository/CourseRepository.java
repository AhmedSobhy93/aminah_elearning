package com.aminah.elearning.repository;

import com.aminah.elearning.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByAuthorId(Long authorId, Pageable pageable);
    // ✅ Search courses by name (case-insensitive)
    List<Course> findByCourseNameContainingIgnoreCase(String courseNamePart);

    Page<Course> findByCourseNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Course> findCourseByCourseNameContainsIgnoreCase(String keyword, Pageable pageable);
    // ✅ (optional) Find all courses a user is enrolled in
    List<Course> findCoursesByEnrollments_User_Id(Long userId);

    @Query("SELECT ce.course FROM CourseEnrollment ce WHERE ce.user.id = :userId")
    List<Course> findCoursesByUserId(@Param("userId") Long userId);

    List<Course> findByAuthorUsernameContainingIgnoreCase(String authorName);

    List<Course> findByAuthorId(Long id);
    Page<Course> findByAuthorUsername(String username, Pageable pageable);
    List<Course> findByPublishedTrue();


}

