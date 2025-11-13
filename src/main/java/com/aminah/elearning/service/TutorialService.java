package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.TutorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TutorialService {
    private final TutorialRepository tutorialRepository;

    public Page<Tutorial> getTutorialsForCourse(Long courseId, Pageable pageable) {
        return tutorialRepository.findByCourseId(courseId, pageable);
    }

    public Tutorial getTutorial(Long id){
        return tutorialRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Tutorial not found"));
    }

    public Tutorial save(Tutorial t){
        return tutorialRepository.save(t);
    }

    public void delete(Long id){
        tutorialRepository.deleteById(id);
    }
//    private final TutorialRepository tutorialRepository;
//    private final CourseRepository courseRepository;
//
//    public Tutorial addTutorialToCourse(Long courseId, Tutorial tutorial) {
//        Course course = courseRepository.findById(courseId)
//                .orElseThrow(() -> new RuntimeException("Course not found"));
//        course.addTutorial(tutorial);
//        return tutorialRepository.save(tutorial);
//    }
//
//    public List<Tutorial> getCourseTutorials(Long courseId) {
//        return tutorialRepository.findByCourseIdOrderByOrderIndex(courseId);
//    }
}




