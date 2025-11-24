package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.TutorialRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.utils.FileUploadUtil;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TutorialRepository tutorialRepository;
    private final UserRepository userRepository;


    public Page<Course> getCoursesByDoctor(String doctorUserName, Pageable pageable) {
        Page<Course> page = courseRepository.findByAuthorUsername(doctorUserName, pageable);
        // convert PersistentBag to ArrayList for safe usage in template
//        page.getContent().forEach(c -> c.setTutorialIds.(List.copyOf(c.getTutorials())));
        return page;
    }

    public Course getCourse(String id) {
        return courseRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Course not found"));
    }

    public Course saveCourse(Course c) {
        return courseRepository.save(c);
    }

    public void deleteCourse(String id) {
        courseRepository.deleteById(id);
    }

//    @Transactional
    public void reorderTutorials(List<String> orderedIds) {
        int index = 0;
        for (String id : orderedIds) {
            Tutorial t = tutorialRepository.findById(id).orElseThrow();
            t.setOrderIndex(index++);
            tutorialRepository.save(t);
        }
    }
////    private final CourseRepository courseRepository;
////    private final TutorialRepository tutorialRepository;
//
//    public Page<Course> getCoursesByDoctor(Long doctorId, Pageable pageable) {
//        return courseRepository.findByAuthorId(doctorId, pageable);
//    }
//
//    public Course getCourse(Long id) {
//        return courseRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Course not found"));
//    }
//
//    public Course saveCourse(Course course) {
//        return courseRepository.save(course);
//    }
//
//    public void deleteCourse(Long id) {
//        courseRepository.deleteById(id);
//    }
//
//    public Page<Tutorial> getTutorials(Long courseId, Pageable pageable) {
//        return tutorialRepository.findByCourseId(courseId, pageable);
//    }
//
//    public Tutorial saveTutorial(Tutorial tutorial) {
//        return tutorialRepository.save(tutorial);
//    }
//
//    public void deleteTutorial(Long id) {
//        tutorialRepository.deleteById(id);
//    }
//    ///   ////////////////////////////
//
//    public List<Course> findAll() {
//        return courseRepository.findAll();
//    }
//
//    public Course findById(Long id) {
//        return courseRepository.findById(id).get();
//    }
//
    public Page<Course> getCourses(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if(keyword == null || keyword.isEmpty())
            return courseRepository.findAll(pageable);
        return courseRepository.findCourseByCourseNameContainsIgnoreCase(keyword, pageable);
    }
//
//
    public Course createCourse(Course course, String drUsername) {
        User dr = userRepository.findByUsername(drUsername)
                .orElseThrow(() -> new RuntimeException("DR not found"));
        course.setAuthorUsername(dr.getUsername());
        return courseRepository.save(course);
    }
//
//    public Course updateCourse(Long courseId, Course updatedCourse) {
//        Course course = courseRepository.findById(courseId)
//                .orElseThrow(() -> new RuntimeException("Course not found"));
//        course.setCourseName(updatedCourse.getCourseName());
//        course.setTitle(updatedCourse.getTitle());
//        course.setDescription(updatedCourse.getDescription());
//        course.setPrice(updatedCourse.getPrice());
//        course.setVideoUrl(updatedCourse.getVideoUrl());
//        course.setPublished(updatedCourse.isPublished());
//        return courseRepository.save(course);
//    }

//    public void deleteCourse(Long courseId) {
//        courseRepository.deleteById(courseId);
//    }

    public List<Course> getCoursesByDR(String drUsername) {
        return courseRepository.findByAuthorUsernameContainingIgnoreCase(drUsername);
    }

//    public Course getCourse(Long id) {
//        return courseRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Course not found"));
//    }

    // Tutorials
    public Tutorial addTutorial(String courseId, Tutorial tutorial, MultipartFile file) throws IOException {
        Course course = getCourse(courseId);
        String path = FileUploadUtil.saveFile("uploads/tutorials", file);
        tutorial.setFilePath(path);
        tutorial.setCourseId(course.getId());
        return tutorialRepository.save(tutorial);
    }
    public List<Tutorial> getTutorials(Course course){
        return tutorialRepository.findByCourseId(course.getId());
    }
    public Page<Course> getCoursesByDR(String drUsername, Pageable pageable) {
        return courseRepository.findByAuthorUsername(drUsername, pageable);
    }

//    public void deleteTutorial(Long tutorialId) {
//        tutorialRepository.deleteById(tutorialId);
//    }
}