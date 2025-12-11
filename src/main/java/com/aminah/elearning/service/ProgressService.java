package com.aminah.elearning.service;

import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.TutorialProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final CourseEnrollmentRepository enrollmentRepo;
    private final TutorialProgressRepository progressRepo;

    public void markTutorialSeen(User user, Tutorial tutorial) {
        TutorialProgress tp = progressRepo.findByUserAndTutorial(user, tutorial)
                .orElseGet(() -> new TutorialProgress(user, tutorial, false));

        tp.setCompleted(true);
        progressRepo.save(tp);

        updateCourseProgress(user, tutorial.getCourse());
    }

    private void updateCourseProgress(User user, Course course) {
        long total = course.getSections().stream()
                .flatMap(s -> s.getTutorials().stream()).count();

        long seen = progressRepo.countCompletedTutorials(user.getId(), course.getId());

        double percent = (seen * 100.0) / total;

        CourseEnrollment ce = enrollmentRepo.findByUserAndCourse(user, course).orElseThrow();
        ce.setProgressPercentage(percent);
        ce.setCompleted(percent >= 100);
        enrollmentRepo.save(ce);
    }
}
