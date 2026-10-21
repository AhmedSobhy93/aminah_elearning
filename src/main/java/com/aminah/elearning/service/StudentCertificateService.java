package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StudentCertificateService {

    private final CourseEnrollmentRepository enrollmentRepo;
    private final EmailService emailService;

    public boolean canGenerate(Long userId, Long courseId) {

        CourseEnrollment e =
                enrollmentRepo.findByUserIdAndCourseId(userId, courseId)
                        .orElseThrow(() -> new RuntimeException("Not enrolled"));

        return Boolean.TRUE.equals(e.getCompleted()) && "SUCCESS".equals(e.getPaymentStatus());
    }

    @Transactional
    public String generateCertificate(Long userId, Long courseId) {
        CourseEnrollment enrollment =
                enrollmentRepo.findForUpdateByUserIdAndCourseId(userId, courseId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Enrollment is required"));

        if (!"SUCCESS".equals(enrollment.getPaymentStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Active enrollment is required");
        }
        if (!Boolean.TRUE.equals(enrollment.getCompleted())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course is not completed yet");
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

    @Transactional(readOnly = true)
    public CertificateDetails getCertificate(Long userId, Long courseId) {
        CourseEnrollment enrollment = enrollmentRepo.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Enrollment is required"));
        if (!Boolean.TRUE.equals(enrollment.getCompleted()) || !"SUCCESS".equals(enrollment.getPaymentStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complete an active enrollment first");
        }
        String title = StringUtils.hasText(enrollment.getCourse().getTitle())
                ? enrollment.getCourse().getTitle() : enrollment.getCourse().getCourseName();
        String name = StringUtils.hasText(enrollment.getUser().getFullName())
                ? enrollment.getUser().getFullName() : enrollment.getUser().getUsername();
        return new CertificateDetails(courseId, name, title, certificateNumber(enrollment),
                Boolean.TRUE.equals(enrollment.getCertificateIssued()));
    }

    public record CertificateDetails(Long courseId, String studentName, String courseTitle,
                                     String number, boolean issued) { }

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
