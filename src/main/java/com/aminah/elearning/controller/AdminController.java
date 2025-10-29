package com.aminah.elearning.controller;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/courses")
    public String list(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/course-list";
    }

    @GetMapping("/courses/new")
    public String createForm(Model model) {
        model.addAttribute("course", new Course());
        return "admin/course-form";
    }

    @PostMapping("/courses/save")
    public String save(@ModelAttribute Course course) {
        if (course.getPrice() == null) course.setPrice(Double.valueOf(0));
        courseRepository.save(course);
        return "redirect:/admin/courses";
    }

    @GetMapping("/courses/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Course c = courseRepository.findById(id).orElseThrow();
        model.addAttribute("course", c);
        return "admin/course-form";
    }

    @GetMapping("/courses/delete/{id}")
    public String delete(@PathVariable Long id) {
        courseRepository.deleteById(id);
        return "redirect:/admin/courses";
    }
}
