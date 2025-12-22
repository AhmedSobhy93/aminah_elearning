package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.Section;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.*;
import com.aminah.elearning.utils.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final TutorialProgressRepository tutorialProgressRepository;

    public Course getCourse(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Course not found"));
    }

    public Course saveCourse(Course c) {
        return courseRepository.save(c);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Course> getCourses(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword == null || keyword.isEmpty()) return courseRepository.findAll(pageable);
        return courseRepository.findCourseByCourseNameContainsIgnoreCase(keyword, pageable);
    }

    public Course createCourse(Course course, User user) {
        User dr = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new RuntimeException("DR not found"));
        course.setAuthor(user);
        return courseRepository.save(course);
    }


    public Page<Course> getPublishedCourses(Pageable pageable) {
        return courseRepository.findByPublishedTrue(pageable);
    }


    public int calculateCourseProgress(User user, Course course) {

        long totalTutorials = course.getSections().stream()
                .flatMap(s -> s.getTutorials().stream())
                .count();

        if (totalTutorials == 0) return 0;

        long completed = tutorialProgressRepository
                .countByUserAndTutorial_Section_Course(user, course);

        return (int) ((completed * 100.0) / totalTutorials);
    }

//    public Tutorial addTutorial(Long sectionId, Tutorial tutorial, MultipartFile file) throws IOException {
//        Section course = getCourse(courseId);
//        String path = FileUploadUtil.saveFile("uploads/tutorials", file);
//        tutorial.setFilePath(path);
//        tutorial.setCourse(course);
//        return tutorialRepository.save(tutorial);
//    }

//    public List<Tutorial> getTutorials(Course course) {
//        return tutorialRepository.getTutorialsByCourse(course.getId());
//    }

    public Page<Course> getCoursesByDR(String drUsername, Pageable pageable) {
        return courseRepository.findByAuthorUsername(drUsername, pageable);
    }


}