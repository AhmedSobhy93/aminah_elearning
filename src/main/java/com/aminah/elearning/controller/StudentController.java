package com.aminah.elearning.controller;

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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final CourseService courseService;
    private final UserService userService;
    private final CourseEnrollmentService enrollmentService;
    private final TutorialService tutorialService;
    private final TutorialService progressService;

    private static final int PAGE_SIZE = 6;

    /* -----------------------------------------------
       COURSE CATALOG (SEARCH + PAGINATION)
       ----------------------------------------------- */
    @GetMapping("/courses")
    public String courseCatalog(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) String keyword,
                                Model model) {

        Page<Course> courses = courseService.getCourses(keyword, page, PAGE_SIZE);

        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", courses.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "student/courses";
    }

    /* -----------------------------------------------
       ENROLL
       ----------------------------------------------- */
    @PostMapping("/enroll/{id}")
    public String enrollCourse(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername());
        Course course = courseService.getCourse(id);

        enrollmentService.enroll(user, course);
        return "redirect:/student/course/" + id;
    }

    /* -----------------------------------------------
       MY COURSES
       ----------------------------------------------- */
    @GetMapping("/my-courses")
    public String myCourses(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {

        User user = userService.findByUsername(userDetails.getUsername());
        Page<CourseEnrollment> enrolled = enrollmentService.getUserEnrollments(
                user.getId(), page, PAGE_SIZE);

        model.addAttribute("enrolledCourses", enrolled);
        return "student/my-courses";
    }

    /* -----------------------------------------------
       COURSE DETAIL (SECTIONS + TUTORIALS)
       ----------------------------------------------- */
    @GetMapping("/course/{id}")
    public String courseDetails(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {

        User user = userService.findByUsername(userDetails.getUsername());
        Course course = courseService.getCourse(id);

        enrollmentService.hasAccess(user, course.getId());

//        progressService.injectProgress(course, user);

        model.addAttribute("course", course);
        return "student/course-detail";
    }

    /* -----------------------------------------------
       TUTORIAL VIEW
       ----------------------------------------------- */
    @GetMapping("/tutorial/{id}")
    public String tutorialView(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {

        User user = userService.findByUsername(userDetails.getUsername());

        Tutorial tutorial = tutorialService.getTutorial(id);
//        progressService.verifySectionLock(t, user);

        TutorialProgress progress = progressService.getProgress( user,tutorial);

        model.addAttribute("tutorial", tutorial);
        model.addAttribute("progress", progress);

        return "student/tutorial-view";
    }

    /* -----------------------------------------------
       MARK TUTORIAL COMPLETED (AJAX)
       ----------------------------------------------- */
    @PostMapping("/tutorial/{id}/complete")
    @ResponseBody
    public Map<String, Object> completeTutorial(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetails userDetails) {

        Tutorial tutorial = tutorialService.getTutorial(id);
        User user = userService.findByUsername(userDetails.getUsername());

        progressService.markComplete(user,tutorial);

        return Map.of("success", true);
    }

    /* -----------------------------------------------
       QUIZ SUBMISSION
       ----------------------------------------------- */
    @PostMapping("/tutorial/{id}/quiz")
    @ResponseBody
    public Map<String, Object> submitQuiz(@PathVariable Long id,
                                          @RequestBody Map<Long, Integer> answers,
                                          @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername());

//        int score = tutorialService.evaluateQuiz(id, answers, user);
        int score = answers.get(id);
        return Map.of("score", score, "success", true);
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