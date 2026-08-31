package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.QuizQuestionRepository;
import com.aminah.elearning.repository.SectionRepository;
import com.aminah.elearning.repository.TutorialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoctorContentAuthorizationServiceTest {

    @Test
    void returnsOnlyCourseResolvedForAuthenticatedAuthor() {
        CourseRepository courses = mock(CourseRepository.class);
        DoctorContentAuthorizationService service = service(courses);
        Course course = new Course(9L);
        User author = new User();
        author.setUsername("doctor-a");
        course.setAuthor(author);
        when(courses.findByIdAndAuthorUsername(9L, "doctor-a")).thenReturn(Optional.of(course));

        assertThat(service.requireOwnedCourse(9L, "doctor-a")).isSameAs(course);
        assertThatThrownBy(() -> service.requireOwnedCourse(9L, "doctor-b"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private DoctorContentAuthorizationService service(CourseRepository courses) {
        return new DoctorContentAuthorizationService(
                courses,
                mock(SectionRepository.class),
                mock(TutorialRepository.class),
                mock(QuizQuestionRepository.class)
        );
    }
}
