package com.aminah.elearning.controller;

import com.aminah.elearning.dto.QuizQuestionDto;
import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DoctorAuthoringJourneyTest {
    private final TutorialService tutorials = mock(TutorialService.class);
    private final DoctorContentAuthorizationService authorization = mock(DoctorContentAuthorizationService.class);
    private final UserRepository users = mock(UserRepository.class);
    private final DoctorController controller = new DoctorController(mock(CourseService.class),
            mock(SectionService.class), tutorials, mock(QuizQuestionService.class),
            mock(CourseEnrollmentService.class), mock(StorageService.class), users,
            mock(CourseEnrollmentService.class), authorization, new ObjectMapper());

    private Section configure() {
        Section section = new Section();
        section.setId(5L);
        when(users.findByUsername("doctor")).thenReturn(Optional.of(new User(2L)));
        when(authorization.requireOwnedSection(5L, "doctor")).thenReturn(section);
        return section;
    }

    @Test
    void sectionDeletionReturnsToTheWorkingCourseRoute() {
        configure();
        assertThat(controller.deleteSection(5L, () -> "doctor")).isEqualTo("redirect:/dr/courses");
    }

    @Test
    void editingQuizReplacesQuestionsAndPreservesRequestedOrderAndPreview() {
        Section section = configure();
        Tutorial tutorial = new Tutorial(9L);
        tutorial.setSection(section);
        tutorial.addQuizQuestion(new QuizQuestion());
        when(authorization.requireOwnedTutorial(9L, "doctor")).thenReturn(tutorial);
        when(tutorials.addTutorialToSection(5L, tutorial)).thenReturn(tutorial);
        var result = controller.addOrUpdateTutorial(5L, "Clinical quiz", TutorialType.QUIZ, null,
                null, true, "[{\"question\":\"Choose\",\"options\":[\"A\",\"B\"],\"correctOptionIndex\":1}]",
                9L, 3, mock(RedirectAttributes.class), () -> "doctor");
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(tutorial.getQuizQuestions()).hasSize(1);
        assertThat(tutorial.getQuizQuestions().getFirst().getCorrectOptionIndex()).isEqualTo(1);
        assertThat(tutorial.getOrderIndex()).isEqualTo(3);
        assertThat(tutorial.isPreview()).isTrue();
    }

    @Test
    void invalidQuizAndMissingUploadAreRejectedBeforeSaving() {
        configure();
        var quiz = controller.addOrUpdateTutorial(5L, "Quiz", TutorialType.QUIZ, null, null,
                false, "[]", null, 0, mock(RedirectAttributes.class), () -> "doctor");
        var pdf = controller.addOrUpdateTutorial(5L, "PDF", TutorialType.PDF, null, null,
                false, null, null, 0, mock(RedirectAttributes.class), () -> "doctor");
        assertThat(quiz.get("success")).isEqualTo(false);
        assertThat(pdf.get("success")).isEqualTo(false);
        verify(tutorials, never()).addTutorialToSection(any(), any());
    }

    @Test
    void courseEditJsonIncludesAllEditableFields() {
        Course course = new Course(4L);
        course.setCourseName("clinical-name");
        course.setPublished(true);
        course.setVideoUrl("https://example.test/video");
        when(authorization.requireOwnedCourse(4L, "doctor")).thenReturn(course);
        var dto = controller.getCourseJson(4L, () -> "doctor");
        assertThat(dto.getCourseName()).isEqualTo("clinical-name");
        assertThat(dto.isPublished()).isTrue();
        assertThat(dto.getVideoUrl()).isEqualTo("https://example.test/video");
    }
}
