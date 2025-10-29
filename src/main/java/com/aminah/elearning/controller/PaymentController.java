package com.aminah.elearning.controller;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.Payment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.PaymentRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.PaymobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired private CourseRepository courseRepository;
    @Autowired private CourseEnrollmentRepository courseEnrollmentRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PaymobService paymobService;

    @GetMapping("/buy/{courseId}")
    public String buy(@PathVariable("courseId") Long courseId, Model model) {
        Course c = courseRepository.findById(courseId).orElseThrow();
        model.addAttribute("course", c);
        return "checkout";
    }

    @PostMapping("/create/{courseId}")
    public String createPayment(@PathVariable("courseId") Long courseId, @RequestParam("username") String username, Model model) {
        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setPassword("NOPASS");
            return userRepository.save(u);
        });

        Course c = courseRepository.findById(courseId).orElseThrow();
        CourseEnrollment e = new CourseEnrollment();
        e.setUser(user);
        e.setCourse(c);
        e.setStatus("PENDING");
        courseEnrollmentRepository.save(e);

        Payment p = new Payment();
        p.setUser(user);
        p.setCourseEnrollment(e);
        p.setAmount(c.getPrice());
        p.setStatus("PENDING");
        p.setGateway("PAYMOB");
        paymentRepository.save(p);

        String iframeUrl = paymobService.createPaymentRequest(p);
        model.addAttribute("iframeUrl", iframeUrl);
        model.addAttribute("payment", p);
        return "paymob-frame";
    }

    @PostMapping("/webhook")
    @ResponseBody
    public String webhook(@RequestBody String payload, @RequestHeader(name="X-Callback-Signature", required=false) String signature) {
        boolean ok = paymobService.handleWebhook(payload, signature);
        return ok ? "OK" : "FAILED";
    }
}
