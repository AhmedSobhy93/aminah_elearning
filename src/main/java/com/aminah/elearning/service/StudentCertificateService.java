package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentCertificateService {

    private final CourseEnrollmentRepository enrollmentRepo;

    public boolean canGenerate(Long userId, Long courseId) {

        CourseEnrollment e =
                enrollmentRepo.findByUserIdAndCourseId(userId, courseId)
                        .orElseThrow(() -> new RuntimeException("Not enrolled"));

        return e.getCompleted();
    }

    public String generateCertificate(Long userId, Long courseId) {
        return "CERT-" + userId + "-" + courseId + "-" + System.currentTimeMillis();
    }
}

