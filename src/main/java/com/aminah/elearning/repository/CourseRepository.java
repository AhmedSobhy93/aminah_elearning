package com.aminah.elearning.repository;

import com.aminah.elearning.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    Page<Course> findByAuthorUsername(String authorUserName, Pageable pageable);
    // ✅ Search courses by name (case-insensitive)
//    List<Course> findByCourseNameContainingIgnoreCase(String courseNamePart);

//    Page<Course> findByCourseNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Course> findCourseByCourseNameContainsIgnoreCase(String keyword, Pageable pageable);
    // ✅ (optional) Find all courses a user is enrolled in
//    List<Course> findCoursesByEnrollments_User_Id(String userId);

//    @Query("SELECT ce.course FROM CourseEnrollment ce WHERE ce.user.id = :userId")
//    List<Course> findCoursesByUserId(@Param("userId") String userId);
//    List<Course> findByAuthorUsername(String username);

    List<Course> findByAuthorUsernameContainingIgnoreCase(String authorName);

//    List<Course> findByAuthorId(String id);
//    Page<Course> findByAuthorUsername(String username, Pageable pageable);
    List<Course> findByPublishedTrue();


}

