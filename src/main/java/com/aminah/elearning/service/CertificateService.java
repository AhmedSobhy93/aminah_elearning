package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CourseEnrollmentRepository enrollmentRepo;

    public boolean canGenerate(User user, Long courseId) {
        return enrollmentRepo.findByUserAndCourse(user, new Course(courseId))
                .map(CourseEnrollment::getCompleted)
                .orElse(false);
    }
}
