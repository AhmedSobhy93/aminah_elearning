package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentCertificateServiceTest {

    @Test
    void issuesStableCertificateForCompletedEnrollment() {
        CourseEnrollmentRepository enrollmentRepository = mock(CourseEnrollmentRepository.class);
        StudentCertificateService service = new StudentCertificateService(enrollmentRepository);
        CourseEnrollment enrollment = completedEnrollment(false);

        when(enrollmentRepository.findByUserIdAndCourseId(7L, 12L))
                .thenReturn(Optional.of(enrollment));

        String certificateNumber = service.generateCertificate(7L, 12L);

        assertThat(certificateNumber).isEqualTo("CERT-7-12-99");
        assertThat(enrollment.getCertificateIssued()).isTrue();
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void repeatedCertificateGenerationKeepsExistingIssuedState() {
        CourseEnrollmentRepository enrollmentRepository = mock(CourseEnrollmentRepository.class);
        StudentCertificateService service = new StudentCertificateService(enrollmentRepository);
        CourseEnrollment enrollment = completedEnrollment(true);

        when(enrollmentRepository.findByUserIdAndCourseId(7L, 12L))
                .thenReturn(Optional.of(enrollment));

        String certificateNumber = service.generateCertificate(7L, 12L);

        assertThat(certificateNumber).isEqualTo("CERT-7-12-99");
        verify(enrollmentRepository, never()).save(enrollment);
    }

    @Test
    void rejectsCertificateForIncompleteEnrollment() {
        CourseEnrollmentRepository enrollmentRepository = mock(CourseEnrollmentRepository.class);
        StudentCertificateService service = new StudentCertificateService(enrollmentRepository);
        CourseEnrollment enrollment = completedEnrollment(false);
        enrollment.setCompleted(false);

        when(enrollmentRepository.findByUserIdAndCourseId(7L, 12L))
                .thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> service.generateCertificate(7L, 12L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Course is not completed yet");
        verify(enrollmentRepository, never()).save(enrollment);
    }

    private CourseEnrollment completedEnrollment(boolean certificateIssued) {
        User user = new User(7L);
        Course course = new Course(12L);
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(99L);
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setCompleted(true);
        enrollment.setCertificateIssued(certificateIssued);
        return enrollment;
    }
}
