package com.aminah.elearning.service;


import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {

        private final CourseEnrollmentRepository enrollmentRepo;
        private final CourseRepository courseRepo;

        public CourseEnrollment enroll(User user, Course course) {
            Optional<CourseEnrollment> existing = enrollmentRepo.findByUserAndCourse(user, course);
            if (existing.isPresent()) return existing.get();

            CourseEnrollment ce = new CourseEnrollment();
            ce.setUser(user);
            ce.setCourse(course);
            ce.setPaymentStatus(course.getPrice() > 0 ? "PENDING" : "SUCCESS");
            return enrollmentRepo.save(ce);
        }

        public boolean hasAccess(User user, Long courseId) {
            return enrollmentRepo.hasAccess(user.getId(), courseId).orElse(false);
        }

        public CourseEnrollment markPaid(Long enrollmentId) {
            CourseEnrollment ce = enrollmentRepo.findById(enrollmentId).orElseThrow();
            ce.setPaymentStatus("SUCCESS");
            return enrollmentRepo.save(ce);
        }

        public  Page<CourseEnrollment>  getEnrollmentsForCourse(Long id,int page,int size) {
             return enrollmentRepo.findByCourseId(id,PageRequest.of(page, size));
        }

        public Boolean existsByCourseIdAndUserId(Long courseId, Long userId) {
            return enrollmentRepo.existsByCourseIdAndUserId(courseId, userId);
        }
    public Page<CourseEnrollment> getUserEnrollments(Long userId, int page, int size) {
        return enrollmentRepo.findByUserId(userId,PageRequest.of(page, size));
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