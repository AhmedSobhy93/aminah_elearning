package com.aminah.elearning.controller;


import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.repository.VideoRepository;
import com.aminah.elearning.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/courses")
@PreAuthorize("hasAuthority('ADMIN')")
@Slf4j
public class CourseController {

//    @Autowired
//    private CourseService courseService;
//
//    @Autowired
//    private CourseEnrollmentRepository courseEnrollmentRepository;
//    @Autowired
//    private final UserRepository userRepo = null;
//    @Autowired
//    private VideoRepository videoRepository;
//    @GetMapping
//    public String list(Model model) {
//        model.addAttribute("courses", courseService.findAll());
//        return "courses/list";
//    }
//
//    @GetMapping("/{id}")
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
//
//    @PostMapping("/{id}/enroll")
//    public String enroll(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
//        // enroll logic: add Enrollment record; redirect back
//        return "redirect:/courses/{id}";
//    }
}
