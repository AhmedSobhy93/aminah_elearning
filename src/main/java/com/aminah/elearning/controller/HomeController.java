package com.aminah.elearning.controller;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
public class HomeController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        var page = courseRepository.findAll();
        model.addAttribute("courses", page);
        return "index";
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable("id") Long id, Model model) {
        Course c = courseRepository.findById(id).orElseThrow();
        model.addAttribute("course", c);
        return "course-detail";
    }
}
