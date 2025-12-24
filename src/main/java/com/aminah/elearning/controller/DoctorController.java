package com.aminah.elearning.controller;

//import com.aminah.elearning.dto.CourseDTOa;
import com.aminah.elearning.dto.CourseDTO;
import com.aminah.elearning.dto.QuizQuestionDto;
import com.aminah.elearning.dto.TutorialDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.GetMapping;

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

    private final ObjectMapper objectMapper; // Jackson mapper

    /* ----------------------------------------------------
     *   LIST COURSES (MAIN PAGE)
     * ---------------------------------------------------- */
    @Transactional(readOnly = true)
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

        boolean enrolled = false; // compute based on user if needed
        int durationHours = c.getSections().size();
        String level = c.getLevel() != null ? c.getLevel().name() : "N/A";
        int progress = 0; // compute from CourseEnrollment if needed

        return new CourseDTO(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getPrice(),
                enrolled,
                durationHours,
                level,
                progress
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

    @Transactional
    @PostMapping("/sections/{sectionId}/tutorials/add")
    @ResponseBody
    public Map<String, Object> addOrUpdateTutorial(
            @PathVariable Long sectionId,
            @RequestParam String title,
            @RequestParam TutorialType type,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String articleContent,
            @RequestParam(required = false) Boolean isPreview,
            @RequestParam(required = false) String quizQuestionsJson,
            @RequestParam(required = false) Long tutorialId,
            RedirectAttributes ra,
            Principal principal
    ) {
        Map<String, Object> resp = new HashMap<>();
        System.out.println("quizQuestionsJson = " + quizQuestionsJson);

        try {
            // 1️⃣ Find user
            var user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2️⃣ Either create new or load existing tutorial
            Tutorial t = (tutorialId != null) ? tutorialService.getTutorial(tutorialId) : new Tutorial();

            t.setTitle(title);
            t.setType(type);
            t.setPreview(Boolean.TRUE.equals(isPreview));

            // 3️⃣ Handle file uploads
            if ((type == TutorialType.VIDEO || type == TutorialType.PDF) && file != null && !file.isEmpty()) {
                String path = storageService.storeFile(file, user.getId(), sectionId, type);

                t.setFilePath(path);
            }

            // 4️⃣ Article content
            if (type == TutorialType.ARTICLE) {
                t.setArticleContent(articleContent);
            }

            Tutorial saved = tutorialService.addTutorialToSection(sectionId, t);

            // Update quiz questions on managed entity

            if (type == TutorialType.QUIZ && quizQuestionsJson != null && !quizQuestionsJson.isBlank()) {
                List<QuizQuestionDto> quizDtos = objectMapper.readValue(
                        quizQuestionsJson, new TypeReference<List<QuizQuestionDto>>() {
                        }
                );

                for (QuizQuestionDto dto : quizDtos) {
                    QuizQuestion q = dto.toEntity(saved);  // ✅ link to persisted tutorial
                    saved.addQuizQuestion(q);
                }
                tutorialService.save(saved);
            }

            ra.addFlashAttribute("success", "Tutorial saved successfully");
            resp.put("success", true);
            resp.put("tutorialId", saved.getId());
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error saving tutorial: " + e.getMessage());
        }

//        return "redirect:/dr/courses";
        return resp;
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
        System.out.println(dto);
        return dto;
    }

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

    @GetMapping("/courses/{courseId}/students-fragment")
    public String getStudentsFragment(@PathVariable Long courseId,
                                      @RequestParam(defaultValue = "0") int page,
                                      Model model) {
        try {
            if (page < 0) page = 0;   // 🔥 Fix the 500 error

            Page<CourseEnrollment> students =
                    courseEnrollmentService.getEnrollmentsForCourse(courseId, page, STUDENTS_PER_PAGE);

            model.addAttribute("studentsPage", students);
            model.addAttribute("students", students.getContent());
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
