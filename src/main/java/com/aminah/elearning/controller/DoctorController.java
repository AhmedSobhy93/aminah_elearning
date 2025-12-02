package com.aminah.elearning.controller;

import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.*;
import com.aminah.elearning.service.CourseEnrollmentService;
import com.aminah.elearning.service.CourseService;
import com.aminah.elearning.service.StorageService;
//import jakarta.annotation.Resource;
import com.aminah.elearning.service.TutorialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/dr")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DR')")
public class DoctorController {

    private final CourseService courseService;
    //    private final CourseRepository courseRepository;
//    private final TutorialRepository tutorialRepository;
    private final TutorialService tutorialService;
    private final UserRepository userRepository;
    private final StorageService storageService;
    //    private final QuizQuestionRepository quizQuestionRepository;
//    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseEnrollmentService courseEnrollmentService;

    private static final int COURSES_PER_PAGE = 5;
    private static final int STUDENTS_PER_PAGE = 5;
    private static final int TUTORIALS_PER_PAGE = 5;

    @GetMapping("/courses")
    public String listCourses(Model model, Principal principal, @RequestParam(defaultValue = "0") int pageCourses) {

        var doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
        var coursesPage = courseService.getCoursesByDR(doctor.getUsername(), PageRequest.of(pageCourses, COURSES_PER_PAGE));

        List<Course> courses = coursesPage.getContent();

        Map<Long, Page<Tutorial>> tutorialsMap = new HashMap<>();
        Map<Long, Page<CourseEnrollment>> studentsMap = new HashMap<>();

        for (Course course : courses) {
            Page<Tutorial> tutPage = tutorialService.getTutorialsForCourse(course.getId(), 0, TUTORIALS_PER_PAGE);
            Page<CourseEnrollment> stuPage = courseEnrollmentService.getUserEnrollments(course.getId(), 0, STUDENTS_PER_PAGE);
            tutorialsMap.put(course.getId(), tutPage);
            studentsMap.put(course.getId(), stuPage);
        }

        model.addAttribute("courses", courses);
        model.addAttribute("coursesPage", coursesPage);
        model.addAttribute("tutorialsMap", tutorialsMap);
        model.addAttribute("studentsMap", studentsMap);
        model.addAttribute("tutorialTypes", TutorialType.values());
        model.addAttribute("currentPage", pageCourses);
        model.addAttribute("totalPages", coursesPage.getTotalPages());
        return "dr/manage-courses";
    }

    // fragment endpoint: tutorials list for a course + page index
    @GetMapping("/courses/{courseId}/tutorials")
    public String getTutorialsFragment(@PathVariable Long courseId, @RequestParam(defaultValue = "0") int page, Model model) {
        Page<Tutorial> tutPage = tutorialService.getTutorialsForCourse(courseId, page, TUTORIALS_PER_PAGE);
        model.addAttribute("tutorialPage", tutPage);
        model.addAttribute("courseId", courseId);
        return "dr/fragments/tutorials :: tutorials-list";
    }

    // fragment endpoint: students list for a course + page index
    @GetMapping("/courses/{courseId}/students-fragment")
    public String getStudentsFragment(@PathVariable Long courseId, @RequestParam(defaultValue = "0") int page, Model model) {
        Page<CourseEnrollment> students = courseEnrollmentService.getEnrollmentsForCourse(courseId, page, STUDENTS_PER_PAGE);
        model.addAttribute("studentsPage", students);
        model.addAttribute("courseId", courseId);
        return "dr/fragments/students :: students-list";
    }

    @PostMapping("/courses/create")
    public String createCourse(@Valid @ModelAttribute Course course, Principal principal, RedirectAttributes ra) {
        try {
            var doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
            course.setAuthor(doctor);
            courseService.saveCourse(course);
            ra.addFlashAttribute("success", "Course created");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    @PostMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes ra) {
        try {
            courseService.deleteCourse(id);
            ra.addFlashAttribute("success", "Course deleted");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    @PostMapping(value = "/courses/{courseId}/tutorials/add", consumes = "multipart/form-data")
    public String addTutorial(@PathVariable Long courseId, @RequestParam String title, @RequestParam TutorialType type, @RequestParam(required = false) MultipartFile file, @RequestParam(required = false) String articleContent, @RequestParam(required = false) List<String> questions, @RequestParam(required = false) List<String> options, @RequestParam(required = false) List<String> answers, Principal principal, RedirectAttributes ra) {

        try {
            var doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
            var course = courseService.getCourse(courseId);

            if (!course.getAuthor().getUsername().equals(doctor.getUsername())) {
                throw new IllegalAccessException("You cannot modify another doctor's course.");
            }

            Tutorial tutorial = new Tutorial();
            tutorial.setTitle(title);
            tutorial.setType(type);
            tutorial.setCourse(course);
//            tutorial.setUserId(doctor.getUsername());
            tutorial.setOrderIndex(course.getTutorials().size());

            /* -------- VIDEO HANDLING -------- */
            if (type == TutorialType.VIDEO) {
                if (file == null || file.isEmpty()) {
                    throw new IllegalArgumentException("Video file required");
                }

                String videoUrl = storageService.storeFile(file, doctor.getId(), courseId, type);
                tutorial.setFilePath(videoUrl);
            }

            /* -------- PDF -------- */
            else if (type == TutorialType.PDF) {
                String url = storageService.storeFile(file, doctor.getId(), courseId, type);
                tutorial.setFilePath(url);
            }

            /* -------- ARTICLE -------- */
            else if (type == TutorialType.ARTICLE) {
                tutorial.setArticleContent(articleContent);
            }

            /* -------- QUIZ -------- */
            else if (type == TutorialType.QUIZ) {
                List<QuizQuestion> list = new ArrayList<>();
                for (int i = 0; i < questions.size(); i++) {
                    QuizQuestion q = new QuizQuestion();
                    q.setTutorial(tutorial);
                    q.setQuestion(questions.get(i));
                    q.setOptions(Arrays.asList(options.get(i).split(",")));
                    q.setAnswer(answers.get(i));
                    list.add(q);
                }
                tutorial.setQuizQuestions(list);
            }

            tutorialService.save(tutorial);

            ra.addFlashAttribute("success", "Tutorial created successfully.");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dr/courses";
    }

    @GetMapping("/tutorial/{id}/view")
    @ResponseBody
    public ResponseEntity<?> viewTutorial(@PathVariable Long id) {

        Tutorial t = tutorialService.getTutorial(id);

        return ResponseEntity.ok(t);
    }

    @PostMapping("/tutorial/{id}/delete")
    public String deleteTutorial(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            tutorialService.delete(id);
            ra.addFlashAttribute("success", "Tutorial deleted");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dr/courses";
    }

//
//    @PostMapping("/tutorials/delete/{id}")
//    public String deleteTutorial(@PathVariable Long id, RedirectAttributes ra) {
//        try {
//            var t = tutorialRepository.findById(id).orElseThrow();
//            // remove reference from course
//            var course = courseRepository.findById(t.getCourse().getId()).orElse(null);
//            if (course != null) {
//                course.getTutorials().remove(id);
//                courseRepository.save(course);
//            }
//            // delete file from storage (safe)
//            if (t.getFilePath() != null) {

    /// /                storageService.delete(t.getFilePath());
//            }
//            tutorialRepository.deleteById(id);
//            ra.addFlashAttribute("success", "Tutorial removed");
//        } catch (Exception e) {
//            ra.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/dr/courses";
//    }
    @PostMapping("/courses/{id}/publish")
    public String togglePublish(@PathVariable Long id) {
        Course c = courseService.getCourse(id);
        c.setPublished(!c.isPublished());
        courseService.saveCourse(c);
        return "redirect:/dr/courses";
    }

}