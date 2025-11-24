package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.TutorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TutorialService {
    private final TutorialRepository tutorialRepository;

    public Page<Tutorial> getTutorialsForCourse(String courseId, Pageable pageable) {
        return tutorialRepository.findByCourseId(courseId, pageable);
    }

    public List<Tutorial> getTutorialsForCourse(String courseId ) {
        return tutorialRepository.findByCourseId(courseId);
    }

    public Tutorial getTutorial(String id){
        return tutorialRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Tutorial not found"));
    }

    public Tutorial save(Tutorial t){
        return tutorialRepository.save(t);
    }

    public void delete(String id){
        tutorialRepository.deleteById(id);
    }

    public Page<Tutorial> getTutorialsForCourse(String courseId, int page, int size) {
        return tutorialRepository.findByCourseId(courseId, PageRequest.of(page, size));
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




