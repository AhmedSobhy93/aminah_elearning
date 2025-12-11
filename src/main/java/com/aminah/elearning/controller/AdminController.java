package com.aminah.elearning.controller;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.repository.VideoRepository;
import com.aminah.elearning.service.CourseEnrollmentService;
import com.aminah.elearning.service.CourseService;
import com.aminah.elearning.service.UserService;
import lombok.RequiredArgsConstructor;
//import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    private final CourseService courseService;

    private final UserRepository userRepo;
    private final CourseEnrollmentService courseEnrollmentService;
    @GetMapping("/users")
    public String getAllUsers(
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        int pageSize = 10; // 10 users per page
        Page<User> usersPage = userService.getUsers(PageRequest.of(page - 1, pageSize));

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());

        return "admin/users/list";
    }


    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/users/user-details";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/users/user-edit";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") User updatedUser) {
        userService.updateUser(id, updatedUser);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/enable/{id}")
    public String enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return "redirect:/admin/users";
    }

    // Admin Courses Management Controllers //


    @GetMapping("/courses")
    public String list(Model model,@RequestParam(name = "page", defaultValue = "1") int page) {

        Page<Course> courses=courseService.getCourses("", page, 5);
        model.addAttribute("totalPages", page);

        model.addAttribute("courses", courses);
        return "admin/course-list";
    }

    @GetMapping("/courses/{id}")
    public String detail(@PathVariable("id") Long id, Model model, Principal principal) {
        Course course = courseService.getCourse(id);//.orElseThrow(() -> new RuntimeException("Course not found"));

        model.addAttribute("course", course);
        if (principal != null) {
            User student = userRepo.findByUsername(principal.getName()).orElse(null);
            boolean enrolled = student != null && courseEnrollmentService.existsByCourseIdAndUserId(student.getId(), course.getId());
            model.addAttribute("enrolled", enrolled);
        }
        return "courses/course-detail";
    }

}
