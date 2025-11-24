package com.aminah.elearning.service;


import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;

    public CourseEnrollment enrollUser(String userId, User user, Course course) {
        if (enrollmentRepository.existsByCourseIdAndCourseEnrollmentUserId(user.getId(), course.getId())) {
            throw new IllegalStateException("User already enrolled in this course");
        }

        CourseEnrollment enrollment = new CourseEnrollment();

        enrollment.setCourseEnrollmentUserId(user.getId());
        enrollment.setCourseId(course.getId());
        enrollment.setPaymentStatus("PENDING");
        enrollment.setProgressPercentage(0.0);
        enrollment.setCompleted(false);

        return enrollmentRepository.save(enrollment);
    }

    public List<CourseEnrollment> getUserEnrollments(String userId) {
        return enrollmentRepository.findByCourseEnrollmentUserId(userId);
    }

    public CourseEnrollment updatePaymentStatus(String enrollmentId, String status) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid enrollment ID"));
        enrollment.setPaymentStatus(status);
        return enrollmentRepository.save(enrollment);
    }

    public CourseEnrollment updateProgress(String enrollmentId, double progress) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid enrollment ID"));
        enrollment.setProgressPercentage(progress);
        if (progress >= 100.0) {
            enrollment.setCompleted(true);
        }
        return enrollmentRepository.save(enrollment);
    }
}