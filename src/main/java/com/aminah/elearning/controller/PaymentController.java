package com.aminah.elearning.controller;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.Payment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.service.CourseEnrollmentService;
import com.aminah.elearning.service.CourseService;
import com.aminah.elearning.service.PaymentService;
import com.aminah.elearning.service.PaymobPaymentService;
import com.aminah.elearning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final String GATEWAY_PAYMOB = "PAYMOB";

    private final CourseService courseService;
    private final CourseEnrollmentService enrollmentService;
    private final PaymentService paymentService;
    private final PaymobPaymentService paymobService;
    private final UserService userService;

    @GetMapping("/buy/{courseId}")
    public String buy(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourse(courseId);
        model.addAttribute("course", course);
        return "checkout";
    }

    @PostMapping("/create/{courseId}")
    public String createPayment(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userService.findByUsername(userDetails.getUsername());
        Course course = courseService.getCourse(courseId);
        CourseEnrollment enrollment = enrollmentService.enroll(user, course);

        if (course.getPrice() == null || course.getPrice() <= 0) {
            enrollmentService.markPaid(enrollment.getId());
            return "redirect:/student/course/" + courseId;
        }

        if (!paymobService.isConfigured()) {
            model.addAttribute("course", course);
            model.addAttribute("error", "Payment gateway is not configured yet.");
            return "checkout";
        }

        Payment payment = paymentService.createPayment(user, enrollment, GATEWAY_PAYMOB);

        String token = paymobService.getAuthToken();
        Integer orderId = paymobService.createOrder(token, course.getPrice(), enrollment.getId());
        payment.setGatewayOrderId(orderId.toString());
        paymentService.updatePaymentStatus(payment, "PENDING");

        String paymentKey = paymobService.generatePaymentKey(token, orderId, course.getPrice(), user.getEmail());
        model.addAttribute("iframeUrl", paymobService.buildIframeUrl(paymentKey));
        model.addAttribute("payment", payment);
        return "paymob-frame";
    }

    @GetMapping("/callback")
    public String callback(@RequestParam Map<String, String> params) {
        if (!paymobService.isValidHmac(params)) {
            return "redirect:/student/my-courses?payment=invalid";
        }

        if (!"true".equalsIgnoreCase(params.get("success")) || !"false".equalsIgnoreCase(params.get("pending"))) {
            return "redirect:/student/my-courses?payment=failed";
        }

        String orderId = params.get("order");
        Payment payment = paymentService.completeGatewayPayment(GATEWAY_PAYMOB, orderId);

        return "redirect:/student/course/" + payment.getCourseEnrollment().getCourse().getId();
    }

    @PostMapping("/webhook")
    @ResponseBody
    public ResponseEntity<String> webhook(@RequestParam Map<String, String> params) {
        if (!paymobService.isValidHmac(params)) {
            return ResponseEntity.status(403).body("INVALID");
        }

        if ("true".equalsIgnoreCase(params.get("success")) && "false".equalsIgnoreCase(params.get("pending"))) {
            paymentService.completeGatewayPayment(GATEWAY_PAYMOB, params.get("order"));
        }

        return ResponseEntity.ok("OK");
    }
}
