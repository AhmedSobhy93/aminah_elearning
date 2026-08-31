package com.aminah.elearning.controller;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.Section;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.CourseEnrollmentService;
import com.aminah.elearning.service.CourseService;
import com.aminah.elearning.service.DoctorContentAuthorizationService;
import com.aminah.elearning.service.QuizQuestionService;
import com.aminah.elearning.service.SectionService;
import com.aminah.elearning.service.StorageService;
import com.aminah.elearning.service.TutorialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoctorControllerSecurityTest {

    @Test
    void createCourseDiscardsSubmittedIdAndRelationshipGraph() {
        CourseService courses = mock(CourseService.class);
        UserRepository users = mock(UserRepository.class);
        DoctorController controller = new DoctorController(
                courses,
                mock(SectionService.class),
                mock(TutorialService.class),
                mock(QuizQuestionService.class),
                mock(CourseEnrollmentService.class),
                mock(StorageService.class),
                users,
                mock(CourseEnrollmentService.class),
                mock(DoctorContentAuthorizationService.class),
                mock(ObjectMapper.class)
        );
        User doctor = new User(22L);
        when(users.findByUsername("doctor-b")).thenReturn(Optional.of(doctor));
        Principal principal = () -> "doctor-b";
        Course submitted = new Course(77L);
        submitted.setEnrollments(new ArrayList<>());
        submitted.getEnrollments().add(new CourseEnrollment());
        submitted.setSections(new ArrayList<>());
        submitted.getSections().add(new Section());

        controller.createCourse(submitted, principal, mock(RedirectAttributes.class));

        assertThat(submitted.getId()).isNull();
        assertThat(submitted.getAuthor()).isSameAs(doctor);
        assertThat(submitted.getEnrollments()).isEmpty();
        assertThat(submitted.getSections()).isEmpty();
        verify(courses).saveCourse(submitted);
    }
}
