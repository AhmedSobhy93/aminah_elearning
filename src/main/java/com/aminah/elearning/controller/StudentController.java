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
@PreAuthorize("hasAuthority('STUDENT')")
@RequiredArgsConstructor
public class StudentController {


    private final CourseService courseService;
    private final CourseEnrollmentService enrollmentService;
    private final StudentService studentService;
    private final UserService userService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    //    private final TutorialRepository  tutorialRepository;
    private final TutorialService tutorialService;
//    private final CourseRepository courseRepository;

    private static final int COURSES_PER_PAGE = 5;

    @GetMapping("/courses")
    public String coursesPage(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, Model model) {
        Page<Course> courses = courseService.getCourses(keyword, page, 5);
        model.addAttribute("courses", courses.getContent());
        model.addAttribute("totalPages", courses.getTotalPages());
        model.addAttribute("currentPage", page);
        return "student/courses";
    }

    @GetMapping("/tutorial/{id}/view")
    @ResponseBody
    public ResponseEntity<?> viewTutorial(@PathVariable Long id) {

        Tutorial t = tutorialService.getTutorial(id);

        return ResponseEntity.ok(t);
    }

    @PostMapping("/tutorial/{id}/complete")
    @ResponseBody
    public ResponseEntity<?> markComplete(@PathVariable Long id, Principal principal) {

        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
//        tutorialService().markCompleted(user.getId(), id);

        return ResponseEntity.ok(Map.of("status", "completed"));
    }

    @PostMapping("/enroll/{id}")
    public String enrollCourse(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Course course = courseService.getCourse(id);
        User student = userService.findByUsername(userDetails.getUsername());
        CourseEnrollment enrollment = enrollmentService.enrollUser(student, course);
        return "redirect:/student/course/" + id;
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
        Page<CourseEnrollment> enrolledCourses = studentService.getEnrolledCourses(student, pageCourses, COURSES_PER_PAGE);
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