package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.TutorialProgress;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.TutorialProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StudentProgressService {

    private final TutorialProgressRepository progressRepo;
    private final CourseEnrollmentRepository enrollmentRepo;
    private final CourseRepository courseRepo;

    public void markTutorialDone(Long userId, Long tutorialId) {

        TutorialProgress progress =
                progressRepo.findByUserIdAndTutorialId(userId, tutorialId)
                        .orElse(new TutorialProgress());

        progress.setUser(new User(userId));
        progress.setTutorial(new Tutorial(tutorialId));
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        progressRepo.save(progress);

        updateCourseProgress(userId, tutorialId);
    }

    public void updateCourseProgress(Long userId, Long tutorialId) {

        Tutorial tutorial = new Tutorial();
        tutorial.setId(tutorialId);

        Long courseId = tutorial.getSection().getCourse().getId();

        CourseEnrollment enrollment =
                enrollmentRepo.findByUserIdAndCourseId(userId, courseId)
                        .orElseThrow(() -> new RuntimeException("Not enrolled"));

        int completed = progressRepo.countByUserIdAndTutorialSectionCourseIdAndCompletedTrue(
                userId, courseId
        );

        int total = courseRepo.findById(courseId)
                .map(c -> c.getSections()
                        .stream()
                        .mapToInt(s -> s.getTutorials().size())
                        .sum())
                .orElse(0);

        double percentage = (completed * 100.0) / total;

        ((CourseEnrollment) enrollment).setProgressPercentage(percentage);

        if (percentage >= 100)
            enrollment.setCompleted(true);

        enrollmentRepo.save(enrollment);
    }
}
