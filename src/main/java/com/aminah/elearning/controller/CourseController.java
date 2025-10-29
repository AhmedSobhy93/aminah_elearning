package com.onlineCourse.controller;


import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.model.Video;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.VideoRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Autowired
    private VideoRepository videoRepository;

    @GetMapping("/courses")
    public String courses(Model model) {
        List<Course> courseList = courseRepository.findAll();
        System.out.println(courseList.toString());
//        model.addAttribute("title", "Courses");
        model.addAttribute("courseList", courseList);
        return "/courses/courses_details";
    }

    @PostMapping("/search")
    public String search(@RequestParam("searchText") String searchText, Model model) {
        List<Course> courseList = courseRepository.findByCourseNameContainingIgnoreCase(searchText);
        model.addAttribute("title", "Courses");
        model.addAttribute("courseList", courseList);
        return "courses_details";
    }

    @GetMapping("/enroll/{courseId}")
    public String enrollUser(HttpSession session, @PathVariable("courseId") int courseId, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Check if already enrolled
        boolean alreadyEnrolled = courseEnrollmentRepository.existsByCourseIdAndUserId(courseId, user.getId());
        if (alreadyEnrolled) {
            model.addAttribute("message", "You are already enrolled in this course.");
            return myCourses(session, model);
        }

        // Paymob Integration (Placeholder)
        String paymentUrl = initiatePaymobPayment(courseId, user);

        // Redirect to Paymob Payment
        return "redirect:" + paymentUrl;
    }

    private String initiatePaymobPayment(int courseId, User user) {
        // In a real app, call Paymob API here and generate payment token
        // For now, redirect to success page
        return "/payment-success/" + courseId;
    }

    @GetMapping("/payment-success/{courseId}")
    public String paymentSuccess(HttpSession session, @PathVariable("courseId") Course course, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(course);
        enrollment.setUser(user);
//        enrollment.setUserName(user.getUsername());
        courseEnrollmentRepository.save(enrollment);

        model.addAttribute("success", "Enrollment completed successfully!");
        return myCourses(session, model);
    }

    @GetMapping("/my-courses")
    public String myCourses(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Course> enrolledCourses = courseRepository.findCoursesByUserId(user.getId());
        model.addAttribute("title", "My Courses");
        model.addAttribute("courseList", enrolledCourses);
        return "courses_details";
    }

    @GetMapping("/course/{id}")
    public String courseDetail(@PathVariable("id") Long id, Model model) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) {
            model.addAttribute("error", "Course not found!");
            return "redirect:/courses";
        }

        List<Video> videos = videoRepository.findByCourseId(id);
        model.addAttribute("course", course);
        model.addAttribute("videos", videos);
        model.addAttribute("title", "Course Details");
        return "courses/course-details";
    }

    // Admin-only features
    @GetMapping("/admin/add-course")
    public String initAddCourse(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("title", "Add Course");
        return "courses/add-course";
    }

    @PostMapping("/admin/add-course")
    public String submitAddCourse(@ModelAttribute("course") Course course, Model model) {
        courseRepository.save(course);
        model.addAttribute("success", course.getCourseName() + " added successfully.");
        return initAddCourse(model);
    }

    @GetMapping("/admin/upload-video/{courseId}")
    public String uploadVideo(@PathVariable("courseId") int courseId, Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("video", new Video());
        model.addAttribute("title", "Upload Video");
        return "courses/upload-video";
    }

    @PostMapping("/admin/upload-video")
    public String handleVideoUpload(@ModelAttribute("video") Video video, Model model) {
        videoRepository.save(video);
        model.addAttribute("success", "Video uploaded successfully!");
        return "redirect:/admin/dashboard";
    }
}
