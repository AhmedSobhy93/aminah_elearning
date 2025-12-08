package com.aminah.elearning.controller;

//import com.aminah.elearning.model.*;
//import com.aminah.elearning.repository.*;
//import com.aminah.elearning.service.*;
/// /import jakarta.annotation.Resource;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.UrlResource;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.security.Principal;
//import java.util.*;
//
//@Controller
//@RequestMapping("/dr")
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('DR')")
//public class DoctorController {
//
//    private final CourseService courseService;
//    //    private final CourseRepository courseRepository;
////    private final TutorialRepository tutorialRepository;
//    private final TutorialService tutorialService;
//    private final UserRepository userRepository;
//    private final StorageService storageService;
//    //    private final QuizQuestionRepository quizQuestionRepository;

import com.aminah.elearning.dto.CourseDTO;
import com.aminah.elearning.dto.QuizQuestionDto;
import com.aminah.elearning.dto.TutorialDto;
import org.springframework.web.bind.annotation.GetMapping;

////    private final CourseEnrollmentRepository courseEnrollmentRepository;
//    private final CourseEnrollmentService courseEnrollmentService;
//    private final SectionService sectionService;
//
//    private static final int COURSES_PER_PAGE = 2;
//    private static final int STUDENTS_PER_PAGE = 5;
//    private static final int TUTORIALS_PER_PAGE = 2;
//
//    @GetMapping("/courses")
//    public String listCourses(Model model, Principal principal, @RequestParam(defaultValue = "0") int pageCourses) {
//        if (pageCourses < 0) pageCourses = 0;
//        var doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
//        var coursesPage = courseService.getCoursesByDR(doctor.getUsername(), PageRequest.of(pageCourses, COURSES_PER_PAGE));
//
//        List<Course> courses = coursesPage.getContent();
//
//        Map<Long, Page<Tutorial>> tutorialsMap = new HashMap<>();
//        Map<Long, Page<CourseEnrollment>> studentsMap = new HashMap<>();
//
//        for (Course course : courses) {
//            Page<Tutorial> tutPage = tutorialService.getTutorialsForSection(course.getId(), 0, TUTORIALS_PER_PAGE);
//            Page<CourseEnrollment> stuPage = courseEnrollmentService.getUserEnrollments(course.getId(), 0, STUDENTS_PER_PAGE);
//            tutorialsMap.put(course.getId(), tutPage);
//            studentsMap.put(course.getId(), stuPage);
//        }
//
//        model.addAttribute("courses", courses);
//        model.addAttribute("coursesPage", coursesPage);
//        model.addAttribute("tutorialsMap", tutorialsMap);
//        model.addAttribute("studentsMap", studentsMap);
//        model.addAttribute("tutorialTypes", TutorialType.values());
//        model.addAttribute("currentPage", pageCourses);
//        model.addAttribute("totalPages", coursesPage.getTotalPages());
//        return "dr/manage-courses";
//    }
//
//    // fragment endpoint: tutorials list for a course + page index
//    @GetMapping("/courses/{sectionId}/tutorials")
//    public String getTutorialsFragment(@PathVariable Long sectionId, @RequestParam(defaultValue = "0") int page, Model model) {
//        if (page < 0) page = 0;
//        Page<Tutorial> tutPage = tutorialService.getTutorialsForSection(sectionId, page, TUTORIALS_PER_PAGE);
//        model.addAttribute("tutorialPage", tutPage);
//        model.addAttribute("sectionId", sectionId);
//        return "dr/fragments/tutorials :: tutorials-list";
//    }
//
//    // fragment endpoint: students list for a course + page index

//
//
//    @PostMapping("/courses/create")
//    public String createCourse(@Valid @ModelAttribute Course course, Principal principal, RedirectAttributes ra) {
//        try {
//            var doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
//            course.setAuthor(doctor);
//            courseService.saveCourse(course);
//            ra.addFlashAttribute("success", "Course created");
//        } catch (Exception e) {
//            ra.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/dr/courses";
//    }
//
//    @PostMapping("/courses/delete/{id}")
//    public String deleteCourse(@PathVariable Long id, RedirectAttributes ra) {
//        try {
//            courseService.deleteCourse(id);
//            ra.addFlashAttribute("success", "Course deleted");
//        } catch (Exception e) {
//            ra.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/dr/courses";
//    }
//    @PostMapping("/sections/{sectionId}/tutorials/add")
//    public String addTutorial(@PathVariable Long sectionId,
//                              @RequestParam String title,
//                              @RequestParam TutorialType type,
//                              @RequestParam(required = false) MultipartFile file,
//                              @RequestParam(required = false) String articleContent,
//                              @PathVariable Long courseId,
//                              Principal principal
//                              ) {
//        var doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
//        Section sec = sectionService.getSection(sectionId);
//        Tutorial t = new Tutorial();
//
//        t.setTitle(title);
//        t.setType(type);
//
//        if(type != TutorialType.ARTICLE && file != null && !file.isEmpty()){
//            try {
//                t.setFilePath(storageService.storeFile(file, doctor.getId(), courseId, type));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        if(type == TutorialType.ARTICLE){
//            t.setArticleContent(articleContent);
//        }
//
//        sec.addTutorial(t);
//        tutorialService.save(t);
//        return "redirect:/dr/courses/manage";
//    }
//
////    @PostMapping(value = "/courses/{courseId}/tutorials/add", consumes = "multipart/form-data")
////    public String addTutorial(@PathVariable Long courseId, @RequestParam String title, @RequestParam TutorialType type, @RequestParam(required = false) MultipartFile file, @RequestParam(required = false) String articleContent, @RequestParam(required = false) List<String> questions, @RequestParam(required = false) List<String> options, @RequestParam(required = false) List<String> answers, Principal principal, RedirectAttributes ra) {
////
////        try {
////            var doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
////            var course = courseService.getCourse(courseId);
////
////            if (!course.getAuthor().getUsername().equals(doctor.getUsername())) {
////                throw new IllegalAccessException("You cannot modify another doctor's course.");
////            }
////
////            Tutorial tutorial = new Tutorial();
////            tutorial.setTitle(title);
////            tutorial.setType(type);
////            tutorial.setCourse(course);
//////            tutorial.setUserId(doctor.getUsername());
////            tutorial.setOrderIndex(course.getTutorials().size());
////
////            /* -------- VIDEO HANDLING -------- */
////            if (type == TutorialType.VIDEO) {
////                if (file == null || file.isEmpty()) {
////                    throw new IllegalArgumentException("Video file required");
////                }
////
////                String videoUrl = storageService.storeFile(file, doctor.getId(), courseId, type);
////                tutorial.setFilePath(videoUrl);
////            }
////
////            /* -------- PDF -------- */
////            else if (type == TutorialType.PDF) {
////                String url = storageService.storeFile(file, doctor.getId(), courseId, type);
////                tutorial.setFilePath(url);
////            }
////
////            /* -------- ARTICLE -------- */
////            else if (type == TutorialType.ARTICLE) {
////                tutorial.setArticleContent(articleContent);
////            }
////
////            /* -------- QUIZ -------- */
////            else if (type == TutorialType.QUIZ) {
////                List<QuizQuestion> list = new ArrayList<>();
////                for (int i = 0; i < questions.size(); i++) {
////                    QuizQuestion q = new QuizQuestion();
////                    q.setTutorial(tutorial);
////                    q.setQuestion(questions.get(i));
////                    q.setOptions(Arrays.asList(options.get(i).split(",")));
////                    q.setAnswer(answers.get(i));
////                    list.add(q);
////                }
////                tutorial.setQuizQuestions(list);
////            }
////
////            tutorialService.save(tutorial);
////
////            ra.addFlashAttribute("success", "Tutorial created successfully.");
////
////        } catch (Exception e) {
////            ra.addFlashAttribute("error", e.getMessage());
////        }
////
////        return "redirect:/dr/courses";
////    }
////
////    @GetMapping("/tutorial/{id}/view")
////    @ResponseBody
////    public ResponseEntity<?> viewTutorial(@PathVariable Long id) {
////
////        Tutorial t = tutorialService.getTutorial(id);
////
////        return ResponseEntity.ok(t);
////    }
//
//
//    @PostMapping("/tutorial/{id}/delete")
//    public String deleteTutorial(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
//        try {
//            tutorialService.delete(id);
//            ra.addFlashAttribute("success", "Tutorial deleted");
//        } catch (Exception e) {
//            ra.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/dr/courses";
//    }
//
////
////    @PostMapping("/tutorials/delete/{id}")
////    public String deleteTutorial(@PathVariable Long id, RedirectAttributes ra) {
////        try {
////            var t = tutorialRepository.findById(id).orElseThrow();
////            // remove reference from course
////            var course = courseRepository.findById(t.getCourse().getId()).orElse(null);
////            if (course != null) {
////                course.getTutorials().remove(id);
////                courseRepository.save(course);
////            }
////            // delete file from storage (safe)
////            if (t.getFilePath() != null) {
//
//    /// /                storageService.delete(t.getFilePath());
////            }
////            tutorialRepository.deleteById(id);
////            ra.addFlashAttribute("success", "Tutorial removed");
////        } catch (Exception e) {
////            ra.addFlashAttribute("error", e.getMessage());
////        }
////        return "redirect:/dr/courses";
////    }
//    @PostMapping("/courses/{id}/publish")
//    public String togglePublish(@PathVariable Long id) {
//        Course c = courseService.getCourse(id);
//        c.setPublished(!c.isPublished());
//        courseService.saveCourse(c);
//        return "redirect:/dr/courses";
//    }
//    @PostMapping("/courses/{courseId}/sections/add")
//    public String addSection(@PathVariable Long courseId, @RequestParam String title) {
//        Course course = courseService.getCourse(courseId);
//        Section s = new Section();
//        s.setTitle(title);
//        course.addSection(s);
//        sectionService.save(s);
//        return "redirect:/dr/courses/manage";
//    }
//
//
//}

////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
//package com.aminah.elearning.controller;

import com.aminah.elearning.model.*;
import com.aminah.elearning.service.*;
import com.aminah.elearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/dr")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DR')")
public class DoctorController {

    private final CourseService courseService;
    private final SectionService sectionService;
    private final TutorialService tutorialService;
    private final QuizQuestionService quizService;
    private final CourseEnrollmentService enrollmentService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final CourseEnrollmentService courseEnrollmentService;
    private static final int COURSES_PER_PAGE = 2;
    private static final int SECTIONS_PER_PAGE = 5;
    private static final int TUTORIALS_PER_PAGE = 5;
    private static final int STUDENTS_PER_PAGE = 5;

    /* ----------------------------------------------------
     *   LIST COURSES (MAIN PAGE)
     * ---------------------------------------------------- */
    @GetMapping("/courses")
    public String listCourses(
            Model model,
            Principal principal,
            @RequestParam(defaultValue = "0") int page
    ) {
        var doctor = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Doctor not found with name: " + principal.getName()));

        Page<Course> coursePage =
                courseService.getCoursesByDR(doctor.getUsername(),
                        PageRequest.of(page, COURSES_PER_PAGE));

        model.addAttribute("coursesPage", coursePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("courses", coursePage.getContent());
        return "dr/manage-courses";
    }

    /* ----------------------------------------------------
     *   CREATE COURSE
     * ---------------------------------------------------- */
    @PostMapping("/courses/create")
    public String createCourse(@ModelAttribute Course course,
                               Principal principal,
                               RedirectAttributes ra) {
        try {
            var doctor = userRepository.findByUsername(principal.getName())
                    .orElseThrow();

            course.setAuthor(doctor);
            courseService.saveCourse(course);
            ra.addFlashAttribute("success", "Course created successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    /* ----------------------------------------------------
     *   DELETE COURSE
     * ---------------------------------------------------- */
    @PostMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id,
                               RedirectAttributes ra) {
        try {
            courseService.deleteCourse(id);
            ra.addFlashAttribute("success", "Course deleted");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    @PostMapping("/courses/edit/{id}")
    public String editCourse(
            @PathVariable Long id,
            @ModelAttribute Course formCourse,
            RedirectAttributes ra
    ) {
        try {
            Course c = courseService.getCourse(id);

            c.setTitle(formCourse.getTitle());
            c.setCourseName(formCourse.getCourseName());
            c.setPrice(formCourse.getPrice());
            c.setPublished(formCourse.isPublished());
            c.setVideoUrl(formCourse.getVideoUrl());
            c.setDescription(formCourse.getDescription());

            courseService.saveCourse(c);
            ra.addFlashAttribute("success", "Course updated successfully");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    @GetMapping("/courses/{id}/json")
    @ResponseBody
    public CourseDTO getCourseJson(@PathVariable Long id) {

        Course c = courseService.getCourse(id);

        return new CourseDTO(
                c.getId(),
                c.getTitle(),
                c.getCourseName(),
                c.getPrice(),
                c.isPublished(),
                c.getVideoUrl(),
                c.getDescription()
        );
    }



    /* ----------------------------------------------------
     *   PUBLISH / UNPUBLISH COURSE
     * ---------------------------------------------------- */
    @PostMapping("/courses/{id}/publish")
    public String publishCourse(@PathVariable Long id) {
        Course c = courseService.getCourse(id);
        c.setPublished(!c.isPublished());
        courseService.saveCourse(c);
        return "redirect:/dr/courses";
    }


    /* ====================================================
     *                     SECTIONS
     * ==================================================== */

    //    @PostMapping("/courses/{courseId}/sections/add")
//    public String addSection(@PathVariable Long courseId,
//                             @RequestParam String title,
//                             RedirectAttributes ra) {
//
//        Course c = courseService.getCourse(courseId);
//        sectionService.createSection(c, title);
//
//        ra.addFlashAttribute("success", "Section added");
//        return "redirect:/dr/courses";
//    }
//
//    @PostMapping("/sections/{id}/delete")
//    public String deleteSection(@PathVariable Long id, RedirectAttributes ra) {
//        try {
//            sectionService.delete(id);
//            ra.addFlashAttribute("success", "Section deleted");
//        } catch (Exception e) {
//            ra.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/dr/courses";
//    }
// Add Section
    @PostMapping("/courses/{courseId}/sections/add")
    public String addSection(@PathVariable Long courseId,
                             @RequestParam String title,
                             @RequestParam(required = false) String description
    ) {
        Course course = courseService.getCourse(courseId);
        Section section = new Section();
        section.setTitle(title);
        section.setDescription(description);
        course.addSection(section);

        sectionService.save(section);
        return "redirect:/dr/courses";
    }

    // Delete Section
    @PostMapping("/sections/delete/{id}")
    public String deleteSection(@PathVariable Long id) {
        sectionService.delete(id);
        return "redirect:/dr/courses/";
    }

    // Edit Section
    @PostMapping("/sections/edit/{id}")
    public String editSection(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description
    ) {
        Section section = sectionService.getSection(id);

        section.setTitle(title);
        section.setDescription(description);

        sectionService.save(section);

        return "redirect:/dr/courses";
    }


    @GetMapping("/courses/{courseId}/sections-fragment")
    public String sectionsFragment(@PathVariable Long courseId,
                                   @RequestParam(defaultValue = "1") int page,
                                   Model model) {
        Course course = courseService.getCourse(courseId);
        List<Section> allSections = course.getSections(); // all sections

        int totalSections = allSections.size();
        int totalPages = (int) Math.ceil((double) totalSections / SECTIONS_PER_PAGE);

        int fromIndex = Math.max(0, (page - 1) * SECTIONS_PER_PAGE);
        int toIndex = Math.min(fromIndex + SECTIONS_PER_PAGE, totalSections);

        List<Section> sections = allSections.subList(fromIndex, toIndex);

        model.addAttribute("sections", sections);
        model.addAttribute("courseId", courseId);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);

        return "dr/fragments/sections :: sections-list";
    }



    /* ====================================================
     *                     TUTORIALS
     * ==================================================== */
    @GetMapping("/sections/{sectionId}/tutorials-fragment")
    public String tutorialsFragment(@PathVariable Long sectionId,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    Model model) {

        if (page < 1) page = 1;

        Page<Tutorial> tutorialsPage = tutorialService.getTutorialsForSection(sectionId, page - 1, 5);

        // Convert to lightweight DTO to avoid LOB access
        List<TutorialDto> tutorials = tutorialsPage
                .map(TutorialDto::from)
                .getContent();

        model.addAttribute("tutorials", tutorials);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", tutorialsPage.getTotalPages());
        model.addAttribute("sectionId", sectionId);

        return "dr/fragments/tutorials :: tutorials-list";
    }




    @PostMapping("/sections/{sectionId}/tutorials/add")
    public String addTutorial(
            @PathVariable Long sectionId,
            @RequestParam String title,
            @RequestParam TutorialType type,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String articleContent,
            RedirectAttributes ra,
            Principal principal
    ) {
        try {
            var dr = userRepository.findByUsername(principal.getName()).orElseThrow();

            Tutorial t = new Tutorial();
            t.setTitle(title);
            t.setType(type);

            /* ------------------ FILE UPLOAD ------------------ */
            if (type != TutorialType.ARTICLE && file != null && !file.isEmpty()) {
                String path = storageService.storeFile(file, dr.getId(), sectionId, type);
                t.setFilePath(path);
            }

            /* ------------------ ARTICLE ------------------ */
            if (type == TutorialType.ARTICLE) {
                t.setArticleContent(articleContent);
            }

            tutorialService.addTutorialToSection(sectionId, t);

            ra.addFlashAttribute("success", "Tutorial added successfully");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    @PostMapping("/tutorial/{id}/edit")
    public String editTutorial(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam TutorialType type,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String articleContent,
            @RequestParam(required = false) String quizQuestionsJson,
            RedirectAttributes ra,
            Principal principal
    ) {
        try {
            Tutorial t = tutorialService.getTutorial(id);

            t.setTitle(title);
            t.setType(type);

            // ---- FILE ----
            if (type != TutorialType.ARTICLE && file != null && !file.isEmpty()) {
                var dr = userRepository.findByUsername(principal.getName()).orElseThrow();
                String path = storageService.storeFile(file, dr.getId(), t.getSection().getId(), type);
                t.setFilePath(path);
            }

            // ---- ARTICLE ----
            if (type == TutorialType.ARTICLE) {
                t.setArticleContent(articleContent);
            }

            // ---- QUIZ ----
            if (type == TutorialType.QUIZ && quizQuestionsJson != null) {
//                tutorialService.updateQuizQuestions(t, quizQuestionsJson);
            }

            tutorialService.save(t);
            ra.addFlashAttribute("success", "Tutorial updated successfully");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dr/courses";
    }


    @PostMapping("/tutorial/{id}/delete-json")
    @ResponseBody
    public Map<String, Object> deleteTutorialJson(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            tutorialService.delete(id);
            response.put("success", true);
            response.put("message", "Tutorial deleted successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }




    @GetMapping("/tutorial/{id}/json")
    @ResponseBody
    public TutorialDto getTutorialJson(@PathVariable Long id) {
        Tutorial t = tutorialService.getTutorial(id);

        TutorialDto dto = TutorialDto.from(t); // safely maps basic fields

        // Map quiz questions if type is QUIZ
        if (t.getType() == TutorialType.QUIZ && t.getQuizQuestions() != null) {
            List<QuizQuestionDto> quizDtos = t.getQuizQuestions().stream()
                    .map(QuizQuestionDto::from) // convert entity -> DTO
                    .toList();
            dto.setQuizQuestions(quizDtos);
        }

        return dto;
    }


//    @GetMapping("/tutorial/{id}/json")
//    @ResponseBody
//    public CourseDTO getTutorialJson(@PathVariable Long id) {
//
//        Tutorial t = tutorialService.getTutorial(id);
//
//        return new TutorialDto(
//                t.getId(),
//                t.getTitle(),
//                t.getSection(),
//                t.getQuizQuestions(),
//                t.getFilePath()
//        );
//    }

    /* ====================================================
     *                     QUIZZES
     * ==================================================== */

    @PostMapping("/tutorial/{tutorialId}/quiz/add")
    public String addQuizQuestion(
            @PathVariable Long tutorialId,
            @RequestParam String question,
            @RequestParam List<String> options,
            @RequestParam String answer,
            RedirectAttributes ra
    ) {
        try {
            Tutorial t = tutorialService.getTutorial(tutorialId);

            QuizQuestion q = new QuizQuestion();
            q.setQuestion(question);
            q.setOptions(options);
//            q.setAnswer(answer);
            q.setTutorial(t);

            quizService.save(q);

            ra.addFlashAttribute("success", "Quiz question added");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dr/courses";
    }

    @PostMapping("/quiz/{id}/delete")
    public String deleteQuizQuestion(@PathVariable Long id, RedirectAttributes ra) {
        try {
            quizService.delete(id);
            ra.addFlashAttribute("success", "Quiz question removed");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dr/courses";
    }


    /* ====================================================
     *                     STUDENTS
     * ==================================================== */

    //    @GetMapping("/courses/{courseId}/students")
//    public String studentsFragment(
//            @PathVariable Long courseId,
//            @RequestParam(defaultValue = "0") int page,
//            Model model
//    ) {
//
//        Page<CourseEnrollment> students =
//                enrollmentService.getEnrollmentsForCourse(courseId, page, STUDENTS_PER_PAGE);
//
//        model.addAttribute("studentsPage", students);
//        model.addAttribute("courseId", courseId);
//
//        return "dr/fragments/students :: students-list";
//    }
    @GetMapping("/courses/{courseId}/students-fragment")
    public String getStudentsFragment(@PathVariable Long courseId,
                                      @RequestParam(defaultValue = "0") int page,
                                      Model model) {
        try {
            if (page < 0) page = 0;   // 🔥 Fix the 500 error

            Page<CourseEnrollment> students =
                    courseEnrollmentService.getEnrollmentsForCourse(courseId, page, STUDENTS_PER_PAGE);

            model.addAttribute("studentsPage", students);
            model.addAttribute("courseId", courseId);

            return "dr/fragments/students :: students-list";
        } catch (Exception ex) {
            ex.printStackTrace(); // <-- print full 500 reason
            throw ex;              // rethrow so Spring shows the error
        }
    }

    /// ///////////Order INdex
    @PostMapping("/sections/reorder")
    @ResponseBody
    public String reorderSections(@RequestBody List<Long> sectionIds) {
        for (int i = 0; i < sectionIds.size(); i++) {
            Section s = sectionService.getSection(sectionIds.get(i));
            s.setOrderIndex(i);
            sectionService.save(s);
        }
        return "OK";
    }

    @PostMapping("/tutorials/reorder")
    @ResponseBody
    public String reorderTutorials(@RequestBody List<Long> tutorialIds) {
        for (int i = 0; i < tutorialIds.size(); i++) {
            Tutorial t = tutorialService.getTutorial(tutorialIds.get(i));
            t.setOrderIndex(i);
            tutorialService.save(t);
        }
        return "OK";
    }


}
