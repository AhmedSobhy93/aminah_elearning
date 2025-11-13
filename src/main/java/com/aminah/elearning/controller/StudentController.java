package com.aminah.elearning.controller;

import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.repository.VideoRepository;
import com.aminah.elearning.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
//    private final CourseRepository courseRepository;

    @GetMapping("/courses")
    public String coursesPage(@RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              Model model){
        Page<Course> courses = courseService.getCourses(keyword, page,5);
        model.addAttribute("courses", courses.getContent());
        model.addAttribute("totalPages", courses.getTotalPages());
        model.addAttribute("currentPage", page);
        return "student/courses";
    }
    // Enroll in a course
//    @PostMapping("/enroll/{courseId}")
//    public String enrollCourse(@PathVariable Long courseId, @AuthenticationPrincipal UserDetails userDetails) {
//        User currentUser = userService.findByUsername(userDetails.getUsername());
//        Course course = courseService.findById(courseId);
//
//        enrollmentService.enrollUser(currentUser.getId(),currentUser, course);
//        return "redirect:/student/courses";
//    }
//    @PostMapping("/enroll/{id}")
//    public String enroll(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
//        User student = userService.findByUsername(userDetails.getUsername());
//        Course course = studentService.getAllPublishedCourses()
//                .stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
//
//        CourseEnrollment enrollment = studentService.enroll(student, course, course.getPrice() == 0.0);
//
//        if (course.getPrice() > 0.0) {
//            Payment payment = paymentService.createPayment(student, enrollment, "STRIPE");
//            // Redirect to payment page (simulate)
//            return "redirect:/student/pay/" + payment.getId();
//        }
//
//        return "redirect:/student/my-courses";
//    }

    @PostMapping("/enroll/{id}")
    public String enrollCourse(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails){
        Course course = courseService.getCourse(id);
        User student = userService.findByUsername(userDetails.getUsername());
        CourseEnrollment enrollment = enrollmentService.enrollUser(student.getId(),student, course);
        return "redirect:/student/course/" + id;
    }

    @PostMapping("/pay/success/{paymentId}")
    public String paymentSuccess(@PathVariable Long paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        paymentService.updatePaymentStatus(payment, "SUCCESS");

        CourseEnrollment enrollment = payment.getCourseEnrollment();
        enrollment.setPaymentStatus("SUCCESS");
        enrollment.setPaymentStatus("ACTIVE");
        studentService.updateProgress(enrollment, 0.0);

        return "redirect:/student/my-courses";
    }


    @GetMapping("/my-courses")
    public String myCourses(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByUsername(userDetails.getUsername());
        List<CourseEnrollment> enrolledCourses = studentService.getEnrolledCourses(student);
        model.addAttribute("enrolledCourses", enrolledCourses);
        return "student/my-courses";
    }

    @GetMapping("/course/{id}")
    public String courseDetails(@PathVariable Long id, Model model, Principal principal){
        Course course = courseService.getCourse(id);
        model.addAttribute("course", course);
        model.addAttribute("tutorials", courseService.getTutorials(course));

        if(principal != null){
            // check enrollment
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            enrollmentService.getUserEnrollments(user.getId()).stream()
                    .filter(e -> e.getCourse().getId().equals(id))
                    .findFirst()
                    .ifPresent(e -> model.addAttribute("enrollment", e));
        }
        return "student/course-details";
    }
    @PostMapping("/pay/{enrollmentId}")
    public String payCourse(@PathVariable Long enrollmentId, Principal principal){
        CourseEnrollment enrollment = enrollmentService.getUserEnrollments(
                        ((User) ((Authentication) principal).getPrincipal()).getId())
                .stream()
                .filter(e -> e.getId().equals(enrollmentId))
                .findFirst().orElseThrow();

        paymentService.processPayment((User) ((Authentication) principal).getPrincipal(), enrollment, enrollment.getCourse().getPrice(), "PAYMOB");
        return "redirect:/student/course/" + enrollment.getCourse().getId();
    }
    // Payment Page
    @GetMapping("/pay/{paymentId}")
    public String payPage(@PathVariable Long paymentId, Model model) {
        Payment payment = paymentService.getPaymentById(paymentId);
        model.addAttribute("payment", payment);
        return "student/payment";
    }

    // Confirm Payment (simulate Stripe/Paymob)
    @PostMapping("/pay/confirm/{paymentId}")
    public String confirmPayment(@PathVariable Long paymentId,
                                 @RequestParam String gateway) {
        Payment payment = paymentService.getPaymentById(paymentId);

        // Simulate actual payment call to Stripe/Paymob here
        // For production, call their SDK or API
        payment.setGateway(gateway);
        payment.setStatus("SUCCESS");
        paymentService.updatePaymentStatus(payment, "SUCCESS");

        // Update enrollment status
        CourseEnrollment enrollment = payment.getCourseEnrollment();
        enrollment.setPaymentStatus("SUCCESS");
        enrollment.setPaymentStatus("ACTIVE");
        studentService.updateProgress(enrollment, 0.0);

        return "redirect:/student/my-courses";
    }

}




//
//@Controller
//@RequestMapping("/student")
//@RequiredArgsConstructor
//public class StudentController {
//
//    private final CourseService courseService;
//
//    private final CourseEnrollmentRepository courseEnrollmentRepository;
//
//    private final UserRepository userRepo;
//
//    private final VideoRepository videoRepository;
//
//    @GetMapping("/courses")
//    public String list(Model model) {
//        model.addAttribute("courses", courseService.findAll());
//        return "courses/list";
//    }
//
//    @GetMapping("/courses/{id}")
//    public String detail(@PathVariable("id") Long id, Model model, Principal principal) {
//        Course course = courseService.findById(id);
//        model.addAttribute("course", course);
//        if (principal != null) {
//            User student = userRepo.findByUsername(principal.getName()).orElse(null);
//            boolean enrolled = student != null && courseEnrollmentRepository.existsByUserAndCourse(student, course);
//            model.addAttribute("enrolled", enrolled);
//        }
//        return "courses/course-detail";
//    }
//}
