package com.aminah.elearning.controller;

import com.aminah.elearning.model.User;
import com.aminah.elearning.service.StudentCertificateService;
import com.aminah.elearning.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentCertificateControllerTest {
    private final StudentCertificateService certificates = mock(StudentCertificateService.class);
    private final UserService users = mock(UserService.class);
    private final StudentCertificateController controller = new StudentCertificateController(certificates, users);
    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("student", "unused");

    @Test
    void issuesOnlyForAuthenticatedStudentsIdentity() {
        when(users.findByUsername("student")).thenReturn(new User(7L));
        assertThat(controller.issue(12L, authentication)).isEqualTo("redirect:/student/certificates/12");
        verify(certificates, never()).getCertificate(any(), any());
        verify(certificates).generateCertificate(7L, 12L);
    }

    @Test
    void downloadRequiresIssuedCertificateAndDoesNotIssueOnGet() {
        when(users.findByUsername("student")).thenReturn(new User(7L));
        when(certificates.getCertificate(7L, 12L)).thenReturn(
                new StudentCertificateService.CertificateDetails(12L, "Student", "Course", "CERT-7-12-99", false));
        assertThatThrownBy(() -> controller.download(12L, authentication, new ExtendedModelMap(), new MockHttpServletResponse()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
        verify(certificates, never()).generateCertificate(any(), any());
    }

    @Test
    void downloadReturnsPrintableAttachmentWithoutIssuance() {
        when(users.findByUsername("student")).thenReturn(new User(7L));
        when(certificates.getCertificate(7L, 12L)).thenReturn(
                new StudentCertificateService.CertificateDetails(12L, "Student", "Course", "CERT-7-12-99", true));
        var response = new MockHttpServletResponse();
        assertThat(controller.download(12L, authentication, new ExtendedModelMap(), response)).isEqualTo("student/certificate");
        assertThat(response.getHeader("Content-Disposition")).isEqualTo("attachment; filename=\"CERT-7-12-99.html\"");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        verify(certificates, never()).generateCertificate(any(), any());
    }
}
