package com.aminah.elearning.controller;

import com.aminah.elearning.dto.CourseViewDTO;
import com.aminah.elearning.dto.QuizQuestionDto;
import com.aminah.elearning.dto.TutorialDto;
import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.*;
import com.aminah.elearning.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final CourseService courseService;
    private final UserService userService;
    private final CourseEnrollmentService enrollmentService;
    private final TutorialService tutorialService;
    private final TutorialProgressRepository tutorialProgressRepository;
    private final TutorialProgressService tutorialProgressService;

    private final SectionService sectionService;
    private static final int PAGE_SIZE = 6;

    /* -----------------------------------------------
       COURSE CATALOG (SEARCH + PAGINATION)
       ----------------------------------------------- */
//    @GetMapping("/courses")
//    public String courseCatalog(@RequestParam(defaultValue = "0") int page,
//                                @RequestParam(required = false) String keyword,
//                                Model model) {
//
//        Page<Course> courses = courseService.getCourses(keyword, page, PAGE_SIZE);
//
//        model.addAttribute("courses", courses);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", courses.getTotalPages());
//        model.addAttribute("keyword", keyword);
//
//        return "student/courses";
//    }

    /* -----------------------------------------------
       ENROLL
       ----------------------------------------------- */
    @PostMapping("/enroll/{id}")
    public String enrollCourse(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername());
        Course course = courseService.getCourse(id);

        enrollmentService.enroll(user, course);
        return "redirect:/student/course/" + id;
    }

    /* -----------------------------------------------
       MY COURSES
       ----------------------------------------------- */
    @GetMapping("/my-courses")
    public String myCourses(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userService.findByUsername(userDetails.getUsername());

        Page<CourseEnrollment> enrollmentsPage =
                enrollmentService.getUserEnrollments(user.getId(), page, PAGE_SIZE);

        List<CourseViewDTO> courses = enrollmentsPage.stream().map(enrollment -> {

            Course course = enrollment.getCourse();

            return new CourseViewDTO(
                    course.getId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getPrice(),
                    course.getLevel() != null ? course.getLevel().name() : "N/A",
                    course.getAuthor() != null ? course.getAuthor().getFullName() : "Unknown",
                    true, // ✅ ALWAYS enrolled
                    enrollment.getProgressPercentage().intValue(),
                    null
            );
        }).toList();

        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", enrollmentsPage.getNumber());
        model.addAttribute("totalPages", enrollmentsPage.getTotalPages());

        return "student/my-courses";
    }

//    @GetMapping("/my-courses")
//    public String myCourses( @RequestParam(defaultValue = "0") int page,@RequestParam(required = false) String keyword, @AuthenticationPrincipal UserDetails userDetails, Model model) {
//
////        User user = userService.findByUsername(userDetails.getUsername());
////        Page<CourseEnrollment> enrolled = enrollmentService.getUserEnrollments(user.getId(), page, PAGE_SIZE);
////
////        model.addAttribute("enrolledCourses", enrolled);
//
//        User user = userDetails != null ? userService.findByUsername(userDetails.getUsername()) : null;
//
//        Page<CourseEnrollment> coursesPage = enrollmentService.getUserEnrollments(user.getId(), page, PAGE_SIZE);
//
//        // Convert to DTOs with enrollment info
//        List<CourseViewDTO> courses = coursesPage.stream().map(course -> {
//            boolean enrolled = false;
//            int progress = 0;
//
//            if (user != null) {
//                CourseEnrollment enrollment =  enrollmentService.findEnrollment(user, course.getId());
//
//                if (enrollment != null) {
//                    enrolled = true;
//                    progress = courseService.calculateCourseProgress(user, course.getCourse());
//                }
//            }
//
//            return new CourseViewDTO(course.getId(), course.getTitle(), course.getDescription(), course.getPrice(), course.getLevel() != null ? course.getLevel().name() : "N/A", course.getAuthor() != null ? course.getAuthor().getFullName() : "Unknown", enrolled, progress, null);
//        }).toList();
//
//        model.addAttribute("courses", courses);
//        model.addAttribute("currentPage", coursesPage.getNumber());
//        model.addAttribute("totalPages", coursesPage.getTotalPages());
//        model.addAttribute("keyword", keyword);
//
//        return "student/my-courses";
//    }

    /* -----------------------------------------------
       COURSE DETAIL (SECTIONS + TUTORIALS)
       ----------------------------------------------- */
    @GetMapping("/courses")
    public String studentCourses(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String keyword, @AuthenticationPrincipal UserDetails userDetails, Model model) {

        User user = userDetails != null ? userService.findByUsername(userDetails.getUsername()) : null;

        Page<Course> coursesPage = courseService.getCourses(keyword, page, PAGE_SIZE);

        // Convert to DTOs with enrollment info
        List<CourseViewDTO> courses = coursesPage.stream().map(course -> {
            boolean enrolled = false;
            int progress = 0;

            if (user != null) {
                CourseEnrollment enrollment = enrollmentService.findEnrollment(user, course.getId());
                if (enrollment != null) {
                    enrolled = true;
                    progress = courseService.calculateCourseProgress(user, course);
                }
            }

            return new CourseViewDTO(course.getId(), course.getTitle(), course.getDescription(), course.getPrice(), course.getLevel() != null ? course.getLevel().name() : "N/A", course.getAuthor() != null ? course.getAuthor().getFullName() : "Unknown", enrolled, progress, null);
        }).toList();

        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", coursesPage.getNumber());
        model.addAttribute("totalPages", coursesPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "student/courses";
    }

    @GetMapping("/course/{id}")
    public String courseDetails(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {

        User user = userService.findByUsername(userDetails.getUsername());
        Course course = courseService.getCourse(id);

        // ✅ check enrollment
        boolean enrolled = course.getEnrollments()
                .stream()
                .anyMatch(e -> e.getUser().getId().equals(user.getId()));

        course.setEnrolled(enrolled);

        // ✅ calculate progress
        int courseProgress = courseService.calculateCourseProgress(user, course);
        course.setProgress(courseProgress);

        course.getSections().forEach(section -> {
            int sectionProgress =
                    sectionService.calculateSectionProgress(user, section);
            section.setProgress(sectionProgress);
        });

        //
        Set<Long> completedTutorialIds =
                tutorialProgressRepository
                        .findCompletedTutorialIds(user);

        course.getSections().forEach(section -> {
            section.getTutorials().forEach(tutorial -> {
                boolean completed =
                        completedTutorialIds.contains(tutorial.getId());
                tutorial.setRead(completed);
            });
        });

        model.addAttribute("course", course);
        return "student/course-details";
    }


//    @GetMapping("/course/{id}")
//    public String courseDetails(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
//
//        Course course = courseService.getCourse(id);
//        User user = userDetails != null ? userService.findByUsername(userDetails.getUsername()) : null;
//        boolean enrolled = user != null && enrollmentService.isEnrolled(user, course.getId());
//
//        int courseProgress = enrolled ? enrollmentService.getProgress(user, course.getId()) : 0;
//
//        // Map sections and tutorials, including tutorial read status
//        List<CourseViewDTO.SectionDTO> sections = course.getSections().stream()
//                .map(section -> {
//                    List<CourseViewDTO.TutorialDTO> tutorials = section.getTutorials().stream()
//                            .map(tut -> new CourseViewDTO.TutorialDTO(
//                                    tut.getId(),
//                                    tut.getTitle(),
//                                    tut.getType().toString(),
//                                    tut.isPreview()
////                                    tut.isRead()
//                            ))
//                            .toList();
//
//                    long incompleteTutorials = tutorials.stream().filter(t -> !t.isPreview()).count();
//                    String status = incompleteTutorials > 0 ? "In Progress" : "Not started";
//
//                    return new CourseViewDTO.SectionDTO(section.getId(), section.getTitle(), tutorials, status,courseProgress);
//                })
//                .toList();
//
//
//        CourseViewDTO courseDTO = new CourseViewDTO(course.getId(), course.getTitle(), course.getDescription(), course.getPrice(), course.getLevel() != null ? course.getLevel().name() : "N/A", course.getAuthor() != null ? course.getAuthor().getFullName() : "Unknown", enrolled, courseProgress, sections);
//
//        model.addAttribute("course", courseDTO);
//        return "student/course-detail";
//    }


    /* -----------------------------------------------
       TUTORIAL VIEW
       ----------------------------------------------- */
//    @GetMapping("/tutorial/{id}")
//    public String tutorialView(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails,
//            Model model) {
//
//        Tutorial tutorial = tutorialService.getTutorial(id);
//        if (tutorial == null) {
//            return "error/404";
//        }
//
//        if (tutorial.getSection() == null || tutorial.getSection().getCourse() == null) {
//            throw new IllegalStateException("Tutorial is not linked to a course");
//        }
//
//        Course course = tutorial.getSection().getCourse();
//        User user = null;
//        boolean enrolled = false;
//
//        if (userDetails != null) {
//            user = userService.findByUsername(userDetails.getUsername());
//            enrolled = enrollmentService.isEnrolled(user, course.getId());
//        }
//
//        // Block if locked
//        if (!enrolled && !tutorial.isPreview()) {
//            return "student/tutorials/tutorial-locked";
//        }
//
//        model.addAttribute("tutorial", tutorial);
//        return "student/tutorials/tutorial-content";
//    }
//
//
//
//    /* -----------------------------------------------
//       MARK TUTORIAL COMPLETED (AJAX)
//       ----------------------------------------------- */
//    @PostMapping("/tutorial/{id}/complete")
//    @ResponseBody
//    public Map<String, Object> completeTutorial(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        Tutorial tutorial = tutorialService.getTutorial(id);
//        User user = userService.findByUsername(userDetails.getUsername());
//
//        tutorial.setRead(true);
//        tutorialService.save(tutorial);
//
//        return Map.of("success", true);
//    }
//
//
//    /* -----------------------------------------------
//       QUIZ SUBMISSION
//       ----------------------------------------------- */
//    @PostMapping("/tutorial/{id}/quiz")
//    @ResponseBody
//    public Map<String, Object> submitQuiz(@PathVariable Long id, @RequestBody Map<Long, Integer> answers, @AuthenticationPrincipal UserDetails userDetails) {
//
//        User user = userService.findByUsername(userDetails.getUsername());
//
////        int score = tutorialService.evaluateQuiz(id, answers, user);
//        int score = answers.get(id);
//        return Map.of("score", score, "success", true);
//    }

    /* ================= VIEW TUTORIAL ================= */
    @GetMapping("/tutorial/{id}")
    public String viewTutorial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Tutorial tutorial = tutorialService.getTutorial(id);
        User user = userService.findByUsername(userDetails.getUsername());

        model.addAttribute("tutorial", tutorial);
        return "student/tutorials/tutorial-content :: content";
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
    /* ================= MARK COMPLETED ================= */
    @PostMapping("/tutorial/{id}/complete")
    @ResponseBody
    public Map<String, Object> markCompleted(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Tutorial tutorial = tutorialService.getTutorial(id);
        User user = userService.findByUsername(userDetails.getUsername());
        System.out.println("Marking tutorial " + tutorial.getId() + " complete for user " + user.getUsername());

        tutorialService.markComplete(user, tutorial);

        return Map.of("success", true);
    }

    /* ================= SUBMIT QUIZ ================= */
    @PostMapping("/tutorial/{id}/quiz")
    @ResponseBody
    public Map<String, Object> submitQuiz(
            @PathVariable Long id,
            @RequestBody Map<Long, Integer> answers, // questionId -> selectedIndex
            @AuthenticationPrincipal UserDetails userDetails) {

        Tutorial tutorial = tutorialService.getTutorial(id);
        User user = userService.findByUsername(userDetails.getUsername());

        int correctCount = 0;
        List<Map<String, Object>> results = new ArrayList<>();

        for (QuizQuestion q : tutorial.getQuizQuestions()) {
            Integer selectedIndex = answers.get(q.getId());

            boolean correct = selectedIndex != null
                    && selectedIndex == q.getCorrectOptionIndex();

            if (correct) correctCount++;

            results.add(Map.of(
                    "questionId", q.getId(),
                    "correct", correct,
                    "correctIndex", q.getCorrectOptionIndex(),
                    "correctAnswer", q.getOptions().get(q.getCorrectOptionIndex())
            ));
        }

        boolean passed = correctCount == tutorial.getQuizQuestions().size();

        if (passed) {
            tutorialProgressService.markComplete(user, tutorial);
        }

        return Map.of(
                "passed", passed,
                "score", correctCount,
                "total", tutorial.getQuizQuestions().size(),
                "results", results
        );
    }


}



/*
@Controller
@RequestMapping("/student")
@PreAuthorize("hasAuthority('STUDENT')")
@RequiredArgsConstructor
public class StudentController {


    private final CourseService courseService;
    private final CourseEnrollmentService enrollmentService;
    private final UserService studentService;
    private final UserService userService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final TutorialService tutorialService;

    private static final int COURSES_PER_PAGE = 5;
    private static final int PAGE_SIZE = 5;

    // List Available Courses
    @GetMapping("/courses")
    public String courses(@RequestParam(required = false) String keyword,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {

        Page<Course> courses = courseService.getCourses(keyword, page, PAGE_SIZE);

        model.addAttribute("courses", courses.getContent());
        model.addAttribute("totalPages", courses.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);

        return "student/courses";
    }

    // Enroll
    @PostMapping("/enroll/{id}")
    public String enroll(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails ud) {
        User student = userService.findByUsername(ud.getUsername());
        Course course = courseService.getCourse(id);
        enrollmentService.enroll(student, course);
        return "redirect:/student/my-courses";
    }

    // List My Courses
    @GetMapping("/my-courses")
    public String myCourses(@AuthenticationPrincipal UserDetails ud,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {

        User student = userService.findByUsername(ud.getUsername());

        Page<CourseEnrollment> enrolled = enrollmentService
                .getUserEnrollments(student.getId(), page, PAGE_SIZE);

        model.addAttribute("enrolledCourses", enrolled);

        return "student/my-courses";
    }

    // Course details
    @GetMapping("/course/{id}")
    public String courseDetails(@PathVariable Long id, Model model) {
        Course course = courseService.getCourse(id);
        model.addAttribute("course", course);
        return "student/course-detail";
    }

    // Tutorial view
    @GetMapping("/tutorial/{id}")
    public String tutorialPage(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails ud,
                               Model model) {

        Tutorial tutorial = tutorialService.getTutorial(id);
        User user = userService.findByUsername(ud.getUsername());
        TutorialProgress prog = tutorialService.getProgress(user, tutorial);

        model.addAttribute("tutorial", tutorial);
        model.addAttribute("progress", prog);

        return "student/tutorial-view";
    }

    // Complete tutorial AJAX
    @PostMapping("/tutorial/{id}/complete")
    @ResponseBody
    public Map<String, Object> completeTutorial(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetails ud) {
        Tutorial tutorial = tutorialService.getTutorial(id);

        User user = userService.findByUsername(ud.getUsername());
        tutorialService.markComplete(user, tutorial);

        return Map.of("success", true);
    }

    // Submit quiz
    @PostMapping("/tutorial/{id}/quiz")
    @ResponseBody
    public Map<String, Object> submitQuiz(@PathVariable Long id,
                                          @RequestBody Map<Long, Integer> answers,
                                          @AuthenticationPrincipal UserDetails ud) {
        Tutorial tutorial = tutorialService.getTutorial(id);

        User user = userService.findByUsername(ud.getUsername());
        int score = tutorialService.submitQuiz(user,tutorial, answers);

        return Map.of("score", score, "success", true);
    }


@PostMapping("/pay/success/{paymentId}")
public String paymentSuccess(@PathVariable Long paymentId) {
    Payment payment = paymentService.getPaymentById(paymentId);
    paymentService.updatePaymentStatus(payment, "SUCCESS");

//        CourseEnrollment enrollment = payment.getCourseEnrollment();
//        enrollment.setPaymentStatus("SUCCESS");
//        enrollment.setPaymentStatus("ACTIVE");
//        studentService.updateProgress(enrollment, 0.0);

    return "redirect:/student/my-courses";
}


@GetMapping("/my-courses")
public String myCourses(Model model, @AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "0") int pageCourses) {
    User student = userService.findByUsername(userDetails.getUsername());
    Page<CourseEnrollment> enrolledCourses = enrollmentService.getUserEnrollments(student.getId(), pageCourses, COURSES_PER_PAGE);
    model.addAttribute("enrolledCourses", enrolledCourses);
    return "student/my-courses";
}

@GetMapping("/course/{id}")
public String courseDetails(@PathVariable Long id, Model model, Principal principal, @RequestParam(defaultValue = "0") int page) {
    Course course = courseService.getCourse(id);
    model.addAttribute("course", course);
//        model.addAttribute("tutorials", courseService.getTutorials(course));

//        if (principal != null) {
//            // check enrollment
//            String username = principal.getName();
//            User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
//            enrollmentService.getUserEnrollments(user.getId(),page,).stream().filter(e -> e.getCourse().getId().equals(id)).findFirst().ifPresent(e -> model.addAttribute("enrollment", e));
//        }
    return "student/course-details";
}

//    @PostMapping("/pay/{enrollmentId}")
//    public String payCourse(@PathVariable Long enrollmentId, Principal principal,@RequestParam(defaultValue = "0") int page) {
//        CourseEnrollment enrollment = enrollmentService.getUserEnrollments(((User) ((Authentication) principal).getPrincipal()).getId()).stream().filter(e -> e.getId().equals(enrollmentId),page,COURSES_PER_PAGE).findFirst().orElseThrow();
//

/// /        paymentService.processPayment((User) ((Authentication) principal).getPrincipal(), enrollment, enrollment.getCourseId(), "PAYMOB");
//        return "redirect:/student/course/" + enrollment.getCourse().getId();
//    }

// Payment Page
@GetMapping("/pay/{paymentId}")
public String payPage(@PathVariable Long paymentId, Model model) {
    Payment payment = paymentService.getPaymentById(paymentId);
    model.addAttribute("payment", payment);
    return "student/payment";
}

// Confirm Payment (simulate Stripe/Paymob)
@PostMapping("/pay/confirm/{paymentId}")
public String confirmPayment(@PathVariable Long paymentId, @RequestParam String gateway) {
    Payment payment = paymentService.getPaymentById(paymentId);

    // Simulate actual payment call to Stripe/Paymob here
    // For production, call their SDK or API
    payment.setGateway(gateway);
    payment.setStatus("SUCCESS");
    paymentService.updatePaymentStatus(payment, "SUCCESS");

    // Update enrollment status
//        CourseEnrollment enrollment = payment.getCourseEnrollmentId();
//        enrollment.setPaymentStatus("SUCCESS");
//        enrollment.setPaymentStatus("ACTIVE");
//        studentService.updateProgress(enrollment, 0.0);

    return "redirect:/student/my-courses";
}
}



 */