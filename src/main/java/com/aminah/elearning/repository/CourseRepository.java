package com.aminah.elearning.repository;

import com.aminah.elearning.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByAuthorUsername(String authorUserName, Pageable pageable);

    Page<Course> findByCourseNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Course> findCourseByCourseNameContainsIgnoreCase(String keyword, Pageable pageable);

    List<Course> findCoursesByEnrollments_User_Id(Long userId);

    Page<Course> findByPublishedTrue(Pageable pageable);

    List<Course> findByPublishedTrue();

    Optional<Course> findByIdAndPublishedTrue(Long id);

    Optional<Course> findByIdAndAuthorUsername(Long id, String username);

    Page<Course> findByCourseNameContainingIgnoreCaseAndPublishedTrue(String keyword, Pageable pageable);

}
