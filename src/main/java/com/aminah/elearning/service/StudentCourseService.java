package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.TutorialProgressRepository;
import com.aminah.elearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class StudentCourseService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final TutorialProgressRepository tutorialProgressRepository;

    public Page<Course> searchCourses(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (keyword == null || keyword.isBlank())
            return courseRepository.findAll(pageable);

        return courseRepository.findCourseByCourseNameContainsIgnoreCase(keyword, pageable);
    }

    public Course getCourseForPreview(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public Optional<CourseEnrollment> getEnrollment(User user,Course course) {
        return enrollmentRepository.findByUserAndCourse(user, course);
    }

    public CourseEnrollment enrollUserInCourse(Long userId, Long courseId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Prevent duplicate enrollment
        Optional<CourseEnrollment> existing =
                enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId());

        if (existing.isPresent())
            return existing.get();

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setPaymentStatus("PENDING");
        enrollment.setProgressPercentage(0.0);
        enrollment.setCompleted(false);

        return enrollmentRepository.save(enrollment);
    }

    public void activateEnrollment(Long enrollmentId) {
        CourseEnrollment e = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        e.setPaymentStatus("SUCCESS");
        enrollmentRepository.save(e);
    }

    public Page<Course> getPublishedCourses(Pageable pageable) {
        return courseRepository.findByPublishedTrue(pageable);
    }
}
