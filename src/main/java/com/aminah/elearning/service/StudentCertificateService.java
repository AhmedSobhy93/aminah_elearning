package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
public class StudentCertificateService {

    private final CourseEnrollmentRepository enrollmentRepo;
    private final EmailService emailService;

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
        if (enrollment.getUser() == null || enrollment.getUser().getId() == null
                || enrollment.getCourse() == null || enrollment.getCourse().getId() == null
                || enrollment.getId() == null) {
            throw new IllegalStateException("Certificate enrollment is incomplete");
        }

        String certificateNumber = certificateNumber(enrollment);
        if (!Boolean.TRUE.equals(enrollment.getCertificateIssued())) {
            enrollment.setCertificateIssued(true);
            enrollmentRepo.save(enrollment);
            sendCertificateEmail(enrollment, certificateNumber);
        }

        return certificateNumber;
    }

    private String certificateNumber(CourseEnrollment enrollment) {
        return "CERT-" + enrollment.getUser().getId()
                + "-" + enrollment.getCourse().getId()
                + "-" + enrollment.getId();
    }

    private void sendCertificateEmail(CourseEnrollment enrollment, String certificateNumber) {
        if (enrollment.getUser() == null || !StringUtils.hasText(enrollment.getUser().getEmail())) {
            return;
        }

        String courseName = "your course";
        if (enrollment.getCourse() != null) {
            if (StringUtils.hasText(enrollment.getCourse().getTitle())) {
                courseName = enrollment.getCourse().getTitle();
            } else if (StringUtils.hasText(enrollment.getCourse().getCourseName())) {
                courseName = enrollment.getCourse().getCourseName();
            }
        }

        String body = "<p>Your certificate for <strong>" + HtmlUtils.htmlEscape(courseName) + "</strong> has been issued.</p>"
                + "<p>Certificate number: " + HtmlUtils.htmlEscape(certificateNumber) + "</p>";

        emailService.sendEmail(enrollment.getUser().getEmail(), "Certificate issued - Aminah E-Learning", body);
    }
}
