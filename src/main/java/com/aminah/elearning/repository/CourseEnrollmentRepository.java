package com.aminah.elearning.repository;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.User;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends MongoRepository<CourseEnrollment, String> {

    // All enrollments for a given user
    List<CourseEnrollment> findByCourseEnrollmentUserId(String userId);

    // Check if a user is already enrolled in a course
    boolean existsByCourseIdAndCourseEnrollmentUserId(String userId, String courseId);

//    boolean existsByCourseIdAndCourseEnrollmentUserId(String courseId, String userId);
//
//    boolean existsByCourseIdAndCourseEnrollmentUserIdAndCourseId(User user, Course course);
    Optional<CourseEnrollment> findByCourseEnrollmentUserIdAndCourseId(String userId, String courseId);
//    List<CourseEnrollment> findAlllByUser(User user);

    List<CourseEnrollment> findByCourseId(String courseId);

}
