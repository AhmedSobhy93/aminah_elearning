package com.aminah.elearning.controller;

import com.aminah.elearning.model.Contact;
import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.ContactRepository;
import com.aminah.elearning.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class HomeController {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ContactRepository contactRepository;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        var page = courseRepository.findAll();
        model.addAttribute("courses", page);
        return "index";
    }

    @GetMapping({ "/about"})
    public String about(Model model) {
//        var page = courseRepository.findAll();
//        model.addAttribute("courses", page);
        return "about";
    }

    @GetMapping({ "/contact"})
    public String contactus(Model model) {
        model.addAttribute("contact", new Contact());
//        User user
//        var page = courseRepository.findAll();
//        model.addAttribute("contactUs", );
        return "contact-us";
    }
    @PostMapping("/contact")
    public String submitContactForm(@ModelAttribute("contact") Contact contact, Model model) {
        try {
            // TODO: send email or save message to DB
            // e.g. contactService.sendContactMessage(contact);
            contactRepository.save(contact);

            model.addAttribute("success", "Thank you for contacting Aminah Hospital. We'll reply soon!");
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while sending your message. Please try again later.");
        }
        return "contact-us";
    }


}
