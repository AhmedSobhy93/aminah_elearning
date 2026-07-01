package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentCertificateService {

    private final CourseEnrollmentRepository enrollmentRepo;

    public boolean canGenerate(Long userId, Long courseId) {

        CourseEnrollment e =
                enrollmentRepo.findByUserIdAndCourseId(userId, courseId)
                        .orElseThrow(() -> new RuntimeException("Not enrolled"));

        return Boolean.TRUE.equals(e.getCompleted());
    }

    @Transactional
    public String generateCertificate(Long userId, Long courseId) {
        CourseEnrollment enrollment =
                enrollmentRepo.findByUserIdAndCourseId(userId, courseId)
                        .orElseThrow(() -> new RuntimeException("Not enrolled"));

        if (!Boolean.TRUE.equals(enrollment.getCompleted())) {
            throw new IllegalStateException("Course is not completed yet");
        }

        if (!Boolean.TRUE.equals(enrollment.getCertificateIssued())) {
            enrollment.setCertificateIssued(true);
            enrollmentRepo.save(enrollment);
        }

        return certificateNumber(enrollment);
    }

    private String certificateNumber(CourseEnrollment enrollment) {
        return "CERT-" + enrollment.getUser().getId()
                + "-" + enrollment.getCourse().getId()
                + "-" + enrollment.getId();
    }
}
