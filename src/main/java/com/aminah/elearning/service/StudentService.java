package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.TutorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final TutorialRepository tutorialRepository;

    public List<Course> getAllPublishedCourses() {
        return courseRepository.findByPublishedTrue();
    }

    public List<CourseEnrollment> getEnrolledCourses(User student) {
        return enrollmentRepository.findByUserId(student.getId());
    }

    public CourseEnrollment enroll(User student, Course course, boolean paid) {
        Optional<CourseEnrollment> opt = enrollmentRepository.findByUserAndCourse(student, course);
        if(opt.isPresent()) return opt.get();

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setUser(student);
        enrollment.setCourse(course);
        enrollment.setPaymentStatus("ACTIVE");
        enrollment.setPaymentStatus(paid ? "SUCCESS" : "PENDING");
        enrollment.setProgressPercentage(0.0);
        enrollment.setCompleted(false);
        return enrollmentRepository.save(enrollment);
    }

    public void updateProgress(CourseEnrollment enrollment, double progress) {
        enrollment.setProgressPercentage(progress);
        if(progress >= 100) {
            enrollment.setCompleted(true);
        }
        enrollmentRepository.save(enrollment);
    }

    public void issueCertificate(CourseEnrollment enrollment) {
        if(enrollment.getCompleted() && !enrollment.getCertificateIssued()) {
            enrollment.setCertificateIssued(true);
            enrollmentRepository.save(enrollment);
            // Generate PDF certificate here (can use iText or JasperReports)
        }
    }

    public List<Tutorial> getCourseTutorials(Course course) {
        return tutorialRepository.findByCourseOrderByOrderIndexAsc(course);
    }
}