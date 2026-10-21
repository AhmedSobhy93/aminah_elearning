package com.aminah.elearning.controller;

import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.TutorialRepository;
import com.aminah.elearning.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TutorialFileControllerTest {
    private final StorageService storage = mock(StorageService.class);
    private final TutorialRepository tutorials = mock(TutorialRepository.class);
    private final UserService users = mock(UserService.class);
    private final CourseEnrollmentService enrollment = mock(CourseEnrollmentService.class);
    private final TutorialFileController controller = new TutorialFileController(storage, tutorials, users, enrollment);
    private final String filePath = "/uploads/2/24/pdfs/clinical.pdf";

    private Tutorial fixture(Role role, boolean access) {
        User user = new User(3L);
        user.setRole(role);
        when(users.findByUsername("qa")).thenReturn(user);
        Course course = new Course(7L);
        course.setPublished(true);
        course.setAuthor(new User(2L));
        Section section = new Section();
        section.setCourse(course);
        Tutorial tutorial = new Tutorial(55L);
        tutorial.setSection(section);
        when(tutorials.findByFilePath(filePath)).thenReturn(Optional.of(tutorial));
        when(enrollment.hasAccess(user, 7L)).thenReturn(access);
        return tutorial;
    }

    @Test
    void deniesAnonymousBeforeConsultingStorage() throws Exception {
        assertThat(controller.serveTutorialFile("2", "24", "pdfs", "clinical.pdf", null).getStatusCode().value()).isEqualTo(401);
        verifyNoInteractions(storage);
    }

    @Test
    void deniesUnenrolledStudentAndOtherDoctorWithoutIssuingLink() throws Exception {
        for (Role role : new Role[]{Role.STUDENT, Role.DR}) {
            fixture(role, false);
            var response = controller.serveTutorialFile("2", "24", "pdfs", "clinical.pdf",
                    new UsernamePasswordAuthenticationToken("qa", "unused", java.util.List.of()));
            assertThat(response.getStatusCode().value()).isEqualTo(403);
        }
        verifyNoInteractions(storage);
    }

    @Test
    void authorizedStudentGetsUncacheablePrivateRedirect() throws Exception {
        fixture(Role.STUDENT, true);
        URI signed = URI.create("https://private.example.com/object?temporary=signature");
        when(storage.getPrivateDownloadUrl(filePath)).thenReturn(Optional.of(signed));
        var response = controller.serveTutorialFile("2", "24", "pdfs", "clinical.pdf",
                new UsernamePasswordAuthenticationToken("qa", "unused", java.util.List.of()));
        assertThat(response.getStatusCode().value()).isEqualTo(302);
        assertThat(response.getHeaders().getLocation()).isEqualTo(signed);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        verify(storage, never()).getFilePath(anyString());
    }

    @Test
    void localBackendStillServesProtectedFile(@TempDir Path directory) throws Exception {
        fixture(Role.STUDENT, true);
        Path pdf = Files.writeString(directory.resolve("clinical.pdf"), "%PDF-fixture");
        when(storage.getPrivateDownloadUrl(filePath)).thenReturn(Optional.empty());
        when(storage.getFilePath(filePath)).thenReturn(pdf);
        var response = controller.serveTutorialFile("2", "24", "pdfs", "clinical.pdf",
                new UsernamePasswordAuthenticationToken("qa", "unused", java.util.List.of()));
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("application/pdf");
    }
}
