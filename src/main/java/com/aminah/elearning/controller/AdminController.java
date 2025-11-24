package com.aminah.elearning.controller;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.repository.VideoRepository;
import com.aminah.elearning.service.CourseService;
import com.aminah.elearning.service.UserService;
import lombok.RequiredArgsConstructor;
//import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private CourseRepository courseRepository;

    // Admin Users Management Controllers //
    @GetMapping("/users")
    public String getAllUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users/list";
    }
    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable String id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/users/user-details";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable String id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/users/user-edit";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable String id, @ModelAttribute("user") User updatedUser) {
        userService.updateUser(id, updatedUser);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/enable/{id}")
    public String enableUser(@PathVariable String id) {
        userService.enableUser(id);
        return "redirect:/admin/users";
    }

    // Admin Courses Management Controllers //


    private final CourseService courseService;

    private final CourseEnrollmentRepository courseEnrollmentRepository;

    private final UserRepository userRepo;

    private final VideoRepository videoRepository;

    @GetMapping("/courses")
    public String list(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "courses/list";
    }

    @GetMapping("/courses/{id}")
    public String detail(@PathVariable("id") String id, Model model, Principal principal) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));

        model.addAttribute("course", course);
        if (principal != null) {
            User student = userRepo.findByUsername(principal.getName()).orElse(null);
            boolean enrolled = student != null && courseEnrollmentRepository.existsByCourseIdAndCourseEnrollmentUserId(student.getId(), course.getId());
            model.addAttribute("enrolled", enrolled);
        }
        return "courses/course-detail";
    }

}
