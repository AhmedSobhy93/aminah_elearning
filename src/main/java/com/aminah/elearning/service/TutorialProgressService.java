package com.aminah.elearning.service;

import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.TutorialProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TutorialProgressService {

    private final CourseEnrollmentRepository enrollmentRepo;
    private final TutorialProgressRepository tutorialProgressRepository;

    @Transactional
    public void markComplete(User user, Tutorial tutorial) {

        TutorialProgress progress =
                tutorialProgressRepository
                        .findByUserAndTutorial(user, tutorial)
                        .orElse(new TutorialProgress(user, tutorial, false));

        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            tutorialProgressRepository.save(progress);
        }
    }


    private void updateCourseProgress(User user, Course course) {
        long total = course.getSections().stream()
                .flatMap(s -> s.getTutorials().stream()).count();

        long seen = tutorialProgressRepository.countCompletedTutorials(user.getId(), course.getId());

        double percent = (seen * 100.0) / total;

        CourseEnrollment ce = enrollmentRepo.findByUserAndCourse(user, course).orElseThrow();
        ce.setProgressPercentage(percent);
        ce.setCompleted(percent >= 100);
        enrollmentRepo.save(ce);
    }
}
