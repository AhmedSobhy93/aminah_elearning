package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.QuizQuestion;
import com.aminah.elearning.model.Section;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.QuizQuestionRepository;
import com.aminah.elearning.repository.SectionRepository;
import com.aminah.elearning.repository.TutorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DoctorContentAuthorizationService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final TutorialRepository tutorialRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    public Course requireOwnedCourse(Long courseId, String username) {
        return courseRepository.findByIdAndAuthorUsername(courseId, username)
                .orElseThrow(() -> denied("Course"));
    }

    public Section requireOwnedSection(Long sectionId, String username) {
        return sectionRepository.findByIdAndCourseAuthorUsername(sectionId, username)
                .orElseThrow(() -> denied("Section"));
    }

    public Tutorial requireOwnedTutorial(Long tutorialId, String username) {
        return tutorialRepository.findByIdAndSectionCourseAuthorUsername(tutorialId, username)
                .orElseThrow(() -> denied("Tutorial"));
    }

    public QuizQuestion requireOwnedQuizQuestion(Long questionId, String username) {
        return quizQuestionRepository.findByIdAndTutorialSectionCourseAuthorUsername(questionId, username)
                .orElseThrow(() -> denied("Quiz question"));
    }

    public List<Section> requireOwnedSections(List<Long> sectionIds, String username) {
        List<Section> sections = sectionRepository.findAllById(sectionIds);
        if (sections.size() != sectionIds.size() || sections.stream().anyMatch(section -> !isOwned(section.getCourse(), username))) {
            throw denied("Section");
        }
        long parentCount = sections.stream().map(section -> section.getCourse().getId()).distinct().count();
        if (parentCount > 1) {
            throw new IllegalArgumentException("Sections must belong to one course");
        }
        return sections;
    }

    public List<Tutorial> requireOwnedTutorials(List<Long> tutorialIds, String username) {
        List<Tutorial> tutorials = tutorialRepository.findAllById(tutorialIds);
        if (tutorials.size() != tutorialIds.size()
                || tutorials.stream().anyMatch(tutorial -> tutorial.getSection() == null
                || !isOwned(tutorial.getSection().getCourse(), username))) {
            throw denied("Tutorial");
        }
        long parentCount = tutorials.stream().map(tutorial -> tutorial.getSection().getId()).distinct().count();
        if (parentCount > 1) {
            throw new IllegalArgumentException("Tutorials must belong to one section");
        }
        return tutorials;
    }

    private boolean isOwned(Course course, String username) {
        return course != null && course.getAuthor() != null && username.equals(course.getAuthor().getUsername());
    }

    private AccessDeniedException denied(String resource) {
        return new AccessDeniedException(resource + " is not owned by the current doctor");
    }
}
