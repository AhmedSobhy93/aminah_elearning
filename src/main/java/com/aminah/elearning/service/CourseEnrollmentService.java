package com.aminah.elearning.service;


import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.TutorialProgressRepository;
import com.aminah.elearning.repository.TutorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepo;
    private final TutorialProgressRepository tutorialProgressRepository;

    public CourseEnrollment enroll(User user, Course course) {
        Optional<CourseEnrollment> existing = enrollmentRepository.findByUserAndCourse(user, course);
        if (existing.isPresent()) return existing.get();

        CourseEnrollment ce = new CourseEnrollment();
        ce.setUser(user);
        ce.setCourse(course);
        ce.setPaymentStatus(course.getPrice() > 0 ? "PENDING" : "SUCCESS");
        return enrollmentRepository.save(ce);
    }

    public boolean hasAccess(User user, Long courseId) {
        return enrollmentRepository.hasAccess(user.getId(), courseId).orElse(false);
    }

    public CourseEnrollment markPaid(Long enrollmentId) {
        CourseEnrollment ce = enrollmentRepository.findById(enrollmentId).orElseThrow();
        ce.setPaymentStatus("SUCCESS");
        return enrollmentRepository.save(ce);
    }

    public Page<CourseEnrollment> getEnrollmentsForCourse(Long id, int page, int size) {
        return enrollmentRepository.findByCourseId(id, PageRequest.of(page, size));
    }

    public Boolean existsByCourseIdAndUserId(Long courseId, Long userId) {
        return enrollmentRepository.existsByCourseIdAndUserId(courseId, userId);
    }

    public Page<CourseEnrollment> getUserEnrollments(Long userId, int page, int size) {
        return enrollmentRepository.findByUserId(userId, PageRequest.of(page, size));
    }

    public boolean isEnrolled(User user, Long courseId) {
        return enrollmentRepository.existsByCourseIdAndUserId(courseId, user.getId());
    }


    // Return the enrollment object (to get progress, etc.)
    public CourseEnrollment findEnrollment(User user, Long courseId) {
        return enrollmentRepository.findByUserAndCourseId(user, courseId).orElse(null);
    }

    public int getProgress(User user, Long courseId) {
        return enrollmentRepository.findByUserAndCourseId(user, courseId).map(CourseEnrollment::getProgressPercentage).map(Double::intValue).orElse(0);
    }

        public boolean isTutorialRead(User user, Tutorial tutorial) {
            return tutorialProgressRepository.existsByUserAndTutorial(user, tutorial);
        }

        // You might also have a method to mark a tutorial as read
        public void markTutorialAsRead(User user, Tutorial tutorial) {
            if (!isTutorialRead(user, tutorial)) {
                TutorialProgress progress = new TutorialProgress(user, tutorial, true);
                tutorialProgressRepository.save(progress);
            }
        }


}


//    private final CourseEnrollmentRepository enrollmentRepository;
//
//    public CourseEnrollment enrollUser(User user, Course course) {
//        if (enrollmentRepository.existsByCourseIdAndUserId(user.getId(), course.getId())) {
//            throw new IllegalStateException("User already enrolled in this course");
//        }
//
//        CourseEnrollment enrollment = new CourseEnrollment();
//
//        enrollment.setUser(user);
//        enrollment.setCourse(course);
//        enrollment.setPaymentStatus("PENDING");
//        enrollment.setProgressPercentage(0.0);
//        enrollment.setCompleted(false);
//
//        return enrollmentRepository.save(enrollment);
//    }
//

//
//    public CourseEnrollment updatePaymentStatus(Long enrollmentId, String status) {
//        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid enrollment ID"));
//        enrollment.setPaymentStatus(status);
//        return enrollmentRepository.save(enrollment);
//    }
//
//    public  Page<CourseEnrollment>  getEnrollmentsForCourse(Long id,int page,int size) {
//        return enrollmentRepository.findByCourseId(id,PageRequest.of(page, size));
//    }
//
//    public CourseEnrollment updateProgress(Long enrollmentId, double progress) {
//        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid enrollment ID"));
//        enrollment.setProgressPercentage(progress);
//        if (progress >= 100.0) {
//            enrollment.setCompleted(true);
//        }
//        return enrollmentRepository.save(enrollment);
//    }
//
//    public int countStudentsInCourse(Long courseId) {
//        return enrollmentRepository.countByCourseId(courseId);
//    }
//}=