package com.aminah.elearning.controller;

import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.TutorialRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.CourseService;
import com.aminah.elearning.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dr")
@RequiredArgsConstructor
public class DoctorController {
    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final TutorialRepository tutorialRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    private static final int COURSES_PER_PAGE = 2;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

    // --- List courses with tutorials ---
    @GetMapping("/courses")
    public String listCourses(Model model, Principal principal,
                              @RequestParam(defaultValue = "0") int page) {
        User doctor = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Page<Course> coursesPage = courseRepository.findByAuthorId(doctor.getId(),
                PageRequest.of(page, COURSES_PER_PAGE));

        // Sort tutorials for each course
        coursesPage.getContent()
                .forEach(c -> c.getTutorials().sort(Comparator.comparingInt(Tutorial::getOrderIndex)));

        model.addAttribute("coursesPage", coursesPage);
        model.addAttribute("courses", coursesPage.getContent());
        model.addAttribute("newCourse", new Course());
        model.addAttribute("tutorialTypes", TutorialType.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursesPage.getTotalPages());

        return "dr/manage-courses";
    }

    // --- Create course ---
    @PostMapping("/courses/create")
    public String createCourse(@Valid @ModelAttribute("newCourse") Course course,
                               Principal principal, RedirectAttributes ra) {
        try {
            User doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
            course.setAuthor(doctor);
            courseService.saveCourse(course);
            ra.addFlashAttribute("success", "Course created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error creating course: " + e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    // --- Delete course ---
    @PostMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes ra) {
        try {
            courseService.deleteCourse(id);
            ra.addFlashAttribute("success", "Course deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting course: " + e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    // --- Add tutorial ---
    @PostMapping("/courses/{courseId}/tutorials/add")
    public String addTutorial(@PathVariable Long courseId,
                              @RequestParam String title,
                              @RequestParam TutorialType type,
                              @RequestParam MultipartFile file,
                              Principal principal, RedirectAttributes ra) {
        try {
            User doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            if (!file.isEmpty()) {
                String path = storageService.storeFile(file, doctor.getId(), courseId, type);

                Tutorial tutorial = new Tutorial();
                tutorial.setTitle(title);
                tutorial.setType(type);
                tutorial.setFilePath(path);
                tutorial.setOrderIndex(course.getTutorials().size());
                tutorial.setCourse(course);

                tutorialRepository.save(tutorial);
            }
            ra.addFlashAttribute("success", "Tutorial added successfully!");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "File upload failed: " + e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error adding tutorial: " + e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    // --- Edit tutorial ---
    @PostMapping("/tutorials/edit/{id}")
    public String editTutorial(@PathVariable Long id,
                               @RequestParam String title,
                               @RequestParam TutorialType type,
                               @RequestParam(required = false) MultipartFile file,
                               Principal principal,
                               RedirectAttributes ra) {
        try {
            Tutorial tutorial = tutorialRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tutorial not found"));

            if (!tutorial.getCourse().getAuthor().getUsername().equals(principal.getName()))
                throw new RuntimeException("Unauthorized");

            tutorial.setTitle(title);
            tutorial.setType(type);

            if (file != null && !file.isEmpty()) {
                String path = storageService.storeFile(file,
                        tutorial.getCourse().getAuthor().getId(),
                        tutorial.getCourse().getId(), type);
                tutorial.setFilePath(path);
            }

            tutorialRepository.save(tutorial);
            ra.addFlashAttribute("success", "Tutorial updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating tutorial: " + e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    // --- Delete tutorial ---
    @PostMapping("/tutorials/delete/{id}")
    public String deleteTutorial(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Tutorial tutorial = tutorialRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tutorial not found"));
            tutorialRepository.delete(tutorial);
            ra.addFlashAttribute("success", "Tutorial deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting tutorial: " + e.getMessage());
        }
        return "redirect:/dr/courses";
    }

    @GetMapping("/courses/{courseId}/students")
    public String viewCourseStudents(@PathVariable Long courseId, Model model) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByCourse(course);
        model.addAttribute("course", course);
        model.addAttribute("enrollments", enrollments);
        return "dr/course-students";
    }

    @PostMapping("/courses/{id}/publish")
    public String togglePublish(@PathVariable Long id) {
        Course course = courseRepository.findById(id).orElseThrow();
        course.setPublished(!course.isPublished());
        courseRepository.save(course);
        return "redirect:/dr/courses";
    }

}

//
//import com.aminah.elearning.model.Course;
//import com.aminah.elearning.model.Tutorial;
//import com.aminah.elearning.model.TutorialType;
//import com.aminah.elearning.model.User;
//import com.aminah.elearning.repository.*;
//import com.aminah.elearning.service.CourseService;
//import com.aminah.elearning.service.StorageService;
//import com.aminah.elearning.service.TutorialService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.io.IOException;
//import java.security.Principal;
//import java.util.*;
//
//@RequestMapping("/dr")
//@RequiredArgsConstructor
//@PreAuthorize("hasAuthority('DR')")
//@Controller
//public class DoctorController {
//
//        private final CourseService courseService;
//        private final TutorialRepository tutorialRepository;
//        private final UserRepository userRepository;
//        private final StorageService storageService;
//        private final CourseRepository courseRepository;
//
//        @GetMapping("/courses")
//        public String listCourses(Model model,
//                                  Principal principal,
//                                  @RequestParam(defaultValue="0") int page) {
//            User dr = userRepository.findByUsername(principal.getName()).orElseThrow();
//            Page<Course> coursesPage = courseRepository.findByAuthorId(dr.getId(), PageRequest.of(page, 2));
//            coursesPage.getContent().forEach(c -> c.getTutorials().sort(Comparator.comparingInt(Tutorial::getOrderIndex)));
//            model.addAttribute("courses", coursesPage.getContent());
//            model.addAttribute("newCourse", new Course());
//            model.addAttribute("currentPage", page);
//            model.addAttribute("totalPages", coursesPage.getTotalPages());
//            return "dr/manage-courses";
//        }
//
//        @PostMapping("/courses/create")
//        public String createCourse(@ModelAttribute("newCourse") Course course, Principal principal){
//            courseService.createCourse(course, principal.getName());
//            return "redirect:/dr/courses";
//        }
//
//        @PostMapping("/courses/delete/{id}")
//        public String deleteCourse(@PathVariable Long id){
//            courseService.deleteCourse(id);
//            return "redirect:/dr/courses";
//        }
//
//        @PostMapping("/courses/{id}/tutorials")
//        public String addTutorial(@PathVariable Long id,
//                                  @RequestParam String title,
//                                  @RequestParam TutorialType type,
//                                  @RequestParam MultipartFile file,
//                                  Principal principal,
//                                  RedirectAttributes redirectAttributes) throws IOException {
//            try {
//                User dr = userRepository.findByUsername(principal.getName()).orElseThrow();
//                Course course = courseRepository.findById(id).orElseThrow();
//                String path = storageService.storeFile(file, dr.getId(), id, type);
//
//                Tutorial t = new Tutorial();
//                t.setTitle(title);
//                t.setType(type);
//                t.setFilePath(path);
//                t.setCourse(course);
//                tutorialRepository.save(t);
//                redirectAttributes.addFlashAttribute("success","Tutorial uploaded");
//            } catch(Exception e){
//                redirectAttributes.addFlashAttribute("error", e.getMessage());
//            }
//            return "redirect:/dr/courses";
//        }
//
//        @PostMapping("/tutorials/delete/{id}")
//        public String deleteTutorial(@PathVariable Long id){
//            tutorialRepository.deleteById(id);
//            return "redirect:/dr/courses";
//        }
//
//        @PostMapping("/tutorials/edit/{id}")
//        public String editTutorial(@PathVariable Long id,
//                                   @RequestParam String title,
//                                   @RequestParam TutorialType type,
//                                   RedirectAttributes redirectAttributes){
//            try {
//                Tutorial t = tutorialRepository.findById(id).orElseThrow();
//                t.setTitle(title);
//                t.setType(type);
//                tutorialRepository.save(t);
//                redirectAttributes.addFlashAttribute("success","Tutorial updated");
//            } catch(Exception e){
//                redirectAttributes.addFlashAttribute("error", e.getMessage());
//            }
//            return "redirect:/dr/courses";
//        }
//
//        @PostMapping("/tutorials/reorder")
//        @ResponseBody
//        public String reorderTutorials(@RequestBody List<Long> ids, Principal principal){
//            int idx=0;
//            for(Long id : ids){
//                Tutorial t = tutorialRepository.findById(id).orElseThrow();
//                t.setOrderIndex(idx++);
//                tutorialRepository.save(t);
//            }
//            return "ok";
//        }
//    }
//
////    private final CourseService courseService;
////    private final TutorialService tutorialService;
////    private final StorageService storageService;
////    private final UserRepository userRepository;
////
////    private static final int COURSES_PER_PAGE = 2;
////    private static final int TUTORIALS_PER_PAGE = 5;
////    private static final int ENROLLMENTS_PER_PAGE = 6;
////
////    @GetMapping("/courses")
////    public String listCourses(Model model, Principal principal, @RequestParam(defaultValue = "0") int page) {
////        var user = userRepository.findByUsername(principal.getName()).orElseThrow();
////        Page<Course> coursesPage = courseService.getCoursesByDoctor(user.getId(), PageRequest.of(page, COURSES_PER_PAGE, Sort.by("createdAt").descending()));
////
////        model.addAttribute("coursesPage", coursesPage);
////        model.addAttribute("courses", coursesPage.getContent());
////        model.addAttribute("newCourse", new Course());
////        model.addAttribute("currentCoursePage", page);
////        return "dr/manage-courses";
////    }
////
////    // Create course
////    @PostMapping("/courses/create")
////    public String createCourse(@Valid @ModelAttribute("newCourse") Course course,
////                               BindingResult br,
////                               Principal principal,
////                               RedirectAttributes ra) {
////        if (br.hasErrors()) {
////            ra.addFlashAttribute("error", "Validation error creating course");
////            return "redirect:/dr/courses";
////        }
////        var user = userRepository.findByUsername(principal.getName()).orElseThrow();
////        course.setAuthor(user);
////        courseService.saveCourse(course);
////        ra.addFlashAttribute("success", "Course created");
////        return "redirect:/dr/courses";
////    }
////
////    // Delete course
////    @PostMapping("/courses/delete/{id}")
////    public String deleteCourse(@PathVariable Long id, RedirectAttributes ra) {
////        try {
////            courseService.deleteCourse(id);
////            ra.addFlashAttribute("success", "Course deleted");
////        } catch (Exception e) {
////            ra.addFlashAttribute("error", "Error deleting course: " + e.getMessage());
////        }
////        return "redirect:/dr/courses";
////    }
////
////    // AJAX: get tutorials page for a course
////    @GetMapping("/courses/{courseId}/tutorials")
////    @ResponseBody
////    public ResponseEntity<?> getTutorials(@PathVariable Long courseId,
////                                          @RequestParam(defaultValue = "0") int page) {
////        Page<Tutorial> p = tutorialService.getTutorialsForCourse(courseId, PageRequest.of(page, TUTORIALS_PER_PAGE, Sort.by("orderIndex").ascending()));
////        // convert to DTO for minimal payload
////        var dto = p.map(t -> Map.of(
////                "id", t.getId(),
////                "title", t.getTitle(),
////                "type", t.getType(),
////                "filePath", t.getFilePath(),
////                "uploadedAt", t.getUploadedAt()
////        ));
////        return ResponseEntity.ok(Map.of(
////                "tutorials", dto.getContent(),
////                "page", p.getNumber(),
////                "totalPages", p.getTotalPages(),
////                "totalElements", p.getTotalElements()
////        ));
////    }
////
////    // AJAX: add tutorial (multipart)
////    @PostMapping("/courses/{courseId}/tutorials")
////    @ResponseBody
////    public ResponseEntity<?> addTutorial(@PathVariable Long courseId,
////                                         @RequestParam String title,
////                                         @RequestParam TutorialType type,
////                                         @RequestParam MultipartFile file,
////                                         Principal principal) {
////        try {
////            var course = courseService.getCourse(courseId);
////            var doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
////
////            if (!course.getAuthor().getId().equals(doctor.getId())) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
////
////            if (title == null || title.trim().length() < 2) return ResponseEntity.badRequest().body(Map.of("error", "Title too short"));
////            if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "File required"));
////            if (file.getSize() > 200L * 1024 * 1024) return ResponseEntity.badRequest().body(Map.of("error", "File too large"));
////
////            String path = storageService.storeFile(file, doctor.getId(), courseId,type);
////            Tutorial t = new Tutorial();
////            t.setTitle(title);
////            t.setType(type);
////            t.setFilePath(path);
////            t.setCourse(course);
////            t.setOrderIndex(course.getTutorials().size());
////            tutorialService.save(t);
////            return ResponseEntity.ok(Map.of("ok", true, "tutorialId", t.getId()));
////        } catch (IOException e) {
////            return ResponseEntity.status(500).body(Map.of("error", "Storage error: " + e.getMessage()));
////        } catch (Exception e) {
////            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
////        }
////    }
////
////    // AJAX: edit tutorial
////    @PostMapping("/tutorials/{id}/edit")
////    @ResponseBody
////    public ResponseEntity<?> editTutorial(@PathVariable Long id,
////                                          @RequestParam String title,
////                                          @RequestParam TutorialType type,
////                                          @RequestParam(required = false) MultipartFile file,
////                                          Principal principal) {
////        try {
////            var tut = tutorialService.getTutorial(id);
////            var doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
////            if (!tut.getCourse().getAuthor().getId().equals(doctor.getId())) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
////
////            if (title == null || title.trim().length() < 2) return ResponseEntity.badRequest().body(Map.of("error", "Title too short"));
////            tut.setTitle(title);
////            tut.setType(type);
////            if (file != null && !file.isEmpty()) {
////                if (file.getSize() > 200L * 1024 * 1024) return ResponseEntity.badRequest().body(Map.of("error", "File too large"));
////                String path = storageService.storeFile(file, doctor.getId(), tut.getCourse().getId(),type);
////                tut.setFilePath(path);
////            }
////            tutorialService.save(tut);
////            return ResponseEntity.ok(Map.of("ok", true));
////        } catch (Exception e) {
////            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
////        }
////    }
////
////    // AJAX: delete tutorial
////    @DeleteMapping("/tutorials/{id}")
////    @ResponseBody
////    public ResponseEntity<?> deleteTutorial(@PathVariable Long id, Principal principal) {
////        try {
////            var tut = tutorialService.getTutorial(id);
////            var doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
////            if (!tut.getCourse().getAuthor().getId().equals(doctor.getId())) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
////            tutorialService.delete(id);
////            return ResponseEntity.ok(Map.of("ok", true));
////        } catch (Exception e) {
////            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
////        }
////    }
////
////    // AJAX: reorder tutorials
////    @PostMapping("/tutorials/reorder")
////    @ResponseBody
////    public ResponseEntity<?> reorderTutorials(@RequestBody List<Long> orderedIds, Principal principal) {
////        try {
////            courseService.reorderTutorials(orderedIds);
////            return ResponseEntity.ok(Map.of("ok", true));
////        } catch (Exception e) {
////            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
////        }
////    }
////
////    // AJAX: get enrollments for a course
////    @GetMapping("/courses/{courseId}/enrollments")
////    @ResponseBody
////    public ResponseEntity<?> getEnrollments(@PathVariable Long courseId,
////                                            @RequestParam(defaultValue = "0") int page) {
////        // Implement using CourseEnrollmentRepository (similar pattern)
////        // For brevity return sample structure:
////        // var enrollmentsPage = enrollmentRepo.findByCourseId(courseId, PageRequest.of(page, ENROLLMENTS_PER_PAGE));
////        // map to DTOs
////        return ResponseEntity.ok(Map.of("students", List.of(), "page", page, "totalPages", 0));
////    }
/////////////////////////////////////////////////////////
////    private final CourseService courseService;
////    private final CourseRepository courseRepository;
////    private final UserRepository userRepository;
////    private final StorageService storageService;
////    private final TutorialRepository   tutorialRepository;
////
////    @GetMapping("/courses")
////    public String listCourses(Model model,
////                              @RequestParam(defaultValue = "0") int coursePage,
////                              @RequestParam(defaultValue = "0") int tutorialPage)  {
////        int coursesPerPage = 2;
////        int tutorialsPerPage = 5;
////
////        List<Course> allCourses = courseRepository.findAll();
////        int totalCourses = allCourses.size();
////        int startCourse = coursePage * coursesPerPage;
////        int endCourse = Math.min(startCourse + coursesPerPage, totalCourses);
////        List<Course> pagedCourses = allCourses.subList(startCourse, endCourse);
////
////        model.addAttribute("courses", pagedCourses);
////        model.addAttribute("coursePage", coursePage);
////        model.addAttribute("totalCoursePages", (int) Math.ceil((double) totalCourses / coursesPerPage));
////
////        // tutorials per course
////        Map<Long, List<Tutorial>> pagedTutorials = new HashMap<>();
////        for (Course course : pagedCourses) {
////            List<Tutorial> allTutorials = course.getTutorials();
////            int totalTutorials = allTutorials.size();
////            int startTutorial = tutorialPage * tutorialsPerPage;
////            int endTutorial = Math.min(startTutorial + tutorialsPerPage, totalTutorials);
////            pagedTutorials.put(course.getId(), allTutorials.subList(startTutorial, endTutorial));
////        }
////
////        model.addAttribute("tutorialsMap", pagedTutorials);
////        model.addAttribute("tutorialPage", tutorialPage);
////        model.addAttribute("tutorialsPerPage", tutorialsPerPage);
////
////        return "dr/manage-courses";
////    }
////
//////    @GetMapping("/courses")
//////    public String listCourses(Model model, Principal principal,
//////                              @RequestParam(defaultValue="0") int page) {
//////        User doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
//////        Page<Course> coursesPage = courseService.getCoursesByDoctor(doctor.getId(), PageRequest.of(page, 2));
//////        model.addAttribute("coursesPage", coursesPage);
//////        model.addAttribute("courses", coursesPage.getContent());
//////        model.addAttribute("newCourse", new Course());
//////        return "dr/manage-courses";
//////    }
////
////    @PostMapping("/courses/create")
////    public String createCourse(@ModelAttribute("newCourse") Course course, Principal principal, RedirectAttributes ra) {
////        try {
////            User doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
////            course.setAuthor(doctor);
////            courseService.saveCourse(course);
////            ra.addFlashAttribute("success", "Course created successfully!");
////        } catch(Exception e) {
////            ra.addFlashAttribute("error", e.getMessage());
////        }
////        return "redirect:/dr/courses";
////    }
////
////    @PostMapping("/courses/delete/{id}")
////    public String deleteCourse(@PathVariable Long id, RedirectAttributes ra) {
////        try {
////            courseService.deleteCourse(id);
////            ra.addFlashAttribute("success", "Course deleted successfully!");
////        } catch(Exception e) {
////            ra.addFlashAttribute("error", e.getMessage());
////        }
////        return "redirect:/dr/courses";
////    }
////
////    @GetMapping("/courses/{courseId}/tutorials")
////    @ResponseBody
////    public Page<Tutorial> getTutorials(@PathVariable Long courseId,
////                                       @RequestParam(defaultValue="0") int page) {
////        return courseService.getTutorials(courseId, PageRequest.of(page, 5));
////    }
////
////
////    @PostMapping("/tutorials/add/{courseId}")
////    public String addTutorial(@PathVariable Long courseId,
////                              @RequestParam String title,
////                              @RequestParam TutorialType type,
////                              @RequestParam MultipartFile file,
////                              Principal principal,
////                              RedirectAttributes ra) throws IOException {
////
////        Course course = courseService.getCourse(courseId);
////
////        Tutorial tutorial = new Tutorial();
////        tutorial.setTitle(title);
////        tutorial.setType(type);
////        tutorial.setCourse(course);
////        tutorial.setOrderIndex(course.getTutorials().size());
////
////        if(!file.isEmpty()) {
////            String path = storageService.storeFile(file, course.getAuthor().getId(), courseId, type);
////            tutorial.setFilePath(path);
////        }
////
////        course.addTutorial(tutorial);
////        courseService.saveCourse(course);
////
////        ra.addFlashAttribute("success", "Tutorial added!");
////        return "redirect:/dr/courses";
////    }
////
////    @PostMapping("/tutorials/delete/{id}")
////    public String deleteTutorial(@PathVariable Long id, RedirectAttributes ra) {
////        try {
////            courseService.deleteTutorial(id);
////            ra.addFlashAttribute("success", "Tutorial deleted!");
////        } catch(Exception e) {
////            ra.addFlashAttribute("error", e.getMessage());
////        }
////        return "redirect:/dr/courses";
////    }
////
////    @PostMapping("/tutorials/edit/{id}")
////    public String editTutorial(@PathVariable Long id,
////                               @RequestParam String title,
////                               @RequestParam TutorialType type,
////                               @RequestParam(required=false) MultipartFile file,
////                               RedirectAttributes ra) throws IOException {
////        try {
////            Optional<Tutorial> t = tutorialRepository.findById(id);
////            t.get().setTitle(title);
////            t.get().setType(type);
////            if(file != null && !file.isEmpty()) {
////                String path = storageService.storeFile(file, t.get().getCourse().getAuthor().getId(), t.get().getCourse().getId(), type);
////                t.get().setFilePath(path);
////            }
////            courseService.saveTutorial(t.orElse(null));
////            ra.addFlashAttribute("success", "Tutorial updated!");
////        } catch(Exception e) {
////            ra.addFlashAttribute("error", e.getMessage());
////        }
////        return "redirect:/dr/courses";
////    }
////
////    @PostMapping("/tutorials/reorder")
////    @ResponseBody
////    public String reorderTutorials(@RequestBody List<Long> tutorialIds, Principal principal) {
//////        int index = 0;
//////        for(Long id : tutorialIds) {
//////            Optional<Tutorial> t = tutorialRepository.findById(id);
//////            t.setOrderIndex(index++);
//////            courseService.saveTutorial(t);
//////        }
////        return "ok";
////    }
//////
////private final CourseService courseService;
////private final TutorialRepository tutorialRepository;
////private final UserRepository userRepository;
////private final TutorialService tutorialService;
////private final CourseRepository courseRepository;
////private final StorageService storageService;
////
//////    @GetMapping("/courses")
//////    public String listCourses(Model model, Principal principal,
//////                              @RequestParam(defaultValue = "0") int page) {
//////        Page<Course> coursesPage = courseService.getCoursesByDR(principal.getName(), PageRequest.of(page, 5));
//////        model.addAttribute("coursesPage", coursesPage);
//////        model.addAttribute("courses", coursesPage.getContent());
//////        model.addAttribute("newCourse", new Course());
//////        return "dr/manage-courses";
//////    }
////
////
////    @PostMapping("/courses/create")
////    public String createCourse(@ModelAttribute("newCourse") Course course,
////                               Principal principal) {
////        courseService.createCourse(course, principal.getName());
////        return "redirect:/dr/courses";
////    }
////
////    @PostMapping("/courses/update/{id}")
////    public String updateCourse(@PathVariable Long id, @ModelAttribute Course course) {
////        courseService.updateCourse(id, course);
////        return "redirect:/dr/courses";
////    }
////
////    @PostMapping("/courses/delete/{id}")
////    public String deleteCourse(@PathVariable Long id) {
////        courseService.deleteCourse(id);
////        return "redirect:/dr/courses";
////    }
////
////    @PostMapping("/courses/{id}/tutorials")
////    public String addTutorial(@PathVariable Long id,
////                              @RequestParam("title") String title,
////                              @RequestParam("type") TutorialType type,
////                              @RequestParam("file") MultipartFile file,Principal principal) throws IOException {
////        Course course = courseService.getCourse(id);
////
////        if (course == null) {
////            throw new NoSuchElementException("Course not found for ID " + id);
////        }
////
////        Tutorial tutorial = new Tutorial();
////        tutorial.setTitle(title);
////        tutorial.setType(type);
////        tutorial.setOrderIndex(course.getTutorials().size());
////        tutorial.setCourse(course);
////
////        // Save uploaded file
////        if (!file.isEmpty()) {
////            String uploadDir = "uploads/tutorials/" + id;
////            Path uploadPath = Paths.get(uploadDir);
////            if (!Files.exists(uploadPath)) {
////                Files.createDirectories(uploadPath);
////            }
////
////            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
////            Path filePath = uploadPath.resolve(fileName);
////            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
////
////            tutorial.setFilePath("/" + uploadDir + "/" + fileName);
////        }
////
////        course.getTutorials().add(tutorial);
////        courseService.createCourse(course, principal.getName()); // will cascade and save tutorial
////
//////        return "redirect:/dr/courses/" + id + "/details";
////        return "redirect:/dr/courses";
////
////    }
//////        Tutorial t = new Tutorial();
//////        t.setTitle(title);
//////        t.setType(type);
//////        courseService.addTutorial(id, t, file);
//////        return "redirect:/dr/courses";
//////    }
////
////    @PostMapping("/tutorials/delete/{id}")
////    public String deleteTutorial(@PathVariable Long id) {
//////        courseService.deleteTutorial(id);
//////        return "redirect:/dr/courses";
////
////        Tutorial tutorial = tutorialRepository.findById(id)
////                .orElseThrow(() -> new NoSuchElementException("Tutorial not found"));
////        Long courseId = tutorial.getCourse().getId();
////        tutorialRepository.delete(tutorial);
////        courseService.deleteTutorial(id);
//////        return "redirect:/dr/courses/" + courseId + "/details";
////        return "redirect:/dr/courses";
////    }
////
////    @GetMapping("/courses")
////    public String getCourses(Model model, Principal principal,@RequestParam(defaultValue = "0") int page) {
////        User doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
////        List<Course> courses = courseRepository.findCoursesByUserId(doctor.getId());
////        Page<Course> coursesPage = courseService.getCoursesByDR(principal.getName(), PageRequest.of(page, 5));
////        model.addAttribute("coursesPage", coursesPage);
////        model.addAttribute("courses", coursesPage.getContent());
////        model.addAttribute("newCourse", new Course());
////        // Sort tutorials by orderIndex
////        courses.forEach(c -> c.getTutorials().sort(Comparator.comparingInt(Tutorial::getOrderIndex)));
////
////        model.addAttribute("courses", courses);
////        return "dr/manage-courses";
////    }
////
////    @PostMapping("/courses/{courseId}/tutorials")
////    public String uploadTutorial(@PathVariable Long courseId,
////                                 @RequestParam String title,
////                                 @RequestParam TutorialType type,
////                                 @RequestParam MultipartFile file,
////                                 Principal principal,
////                                 RedirectAttributes redirectAttributes, Pageable pageable) throws IOException {
////        try {
////            User doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
////            Optional<Course> course = courseRepository.findById(courseId);
////
////            if (file.isEmpty()) throw new RuntimeException("Select a file");
////            if (file.getSize() > 200*1024*1024) throw new RuntimeException("File too large");
////
////            String filePath = storageService.storeFile(file, doctor.getId(), courseId, type);
////
////            Tutorial tutorial = new Tutorial();
////            tutorial.setTitle(title);
////            tutorial.setType(type);
////            tutorial.setFilePath(filePath);
////            tutorial.setCourse(course.get());
//////            tutorial.setOrderIndex(course.get().size());
////            tutorialRepository.save(tutorial);
////
////            redirectAttributes.addFlashAttribute("success", "Tutorial uploaded.");
////        } catch (Exception e) {
////            redirectAttributes.addFlashAttribute("error", e.getMessage());
////        }
////        return "redirect:/dr/courses";
////    }
////
////    @PostMapping("/tutorials/edit/{id}")
////    public String editTutorial(@PathVariable Long id,
////                               @RequestParam String title,
////                               @RequestParam TutorialType type,
////                               @RequestParam(required=false) MultipartFile file,
////                               Principal principal,
////                               RedirectAttributes redirectAttributes) throws IOException {
////        try {
////            Tutorial tutorial = tutorialRepository.findById(id).orElseThrow();
////
////            if (!tutorial.getCourse().getAuthor().getUsername().equals(principal.getName()))
////                throw new RuntimeException("Unauthorized");
////
////            tutorial.setTitle(title);
////            tutorial.setType(type);
////
////            if (file != null && !file.isEmpty()) {
////                if (file.getSize() > 200*1024*1024) throw new RuntimeException("File too large");
////                String filePath = storageService.storeFile(file,
////                        tutorial.getCourse().getAuthor().getId(),
////                        tutorial.getCourse().getId(), type);
////                tutorial.setFilePath(filePath);
////            }
////
////            tutorialRepository.save(tutorial);
////            redirectAttributes.addFlashAttribute("success", "Tutorial updated.");
////        } catch (Exception e) {
////            redirectAttributes.addFlashAttribute("error", e.getMessage());
////        }
////        return "redirect:/dr/courses";
////    }
////
////    @PostMapping("/tutorials/reorder")
////    @ResponseBody
////    public String reorderTutorials(@RequestBody List<Long> tutorialIds, Principal principal) {
////        User doctor = userRepository.findByUsername(principal.getName()).orElseThrow();
////
////        int index = 0;
////        for (Long id : tutorialIds) {
////            Tutorial t = tutorialRepository.findById(id).orElseThrow();
////            if (!t.getCourse().getAuthor().getId().equals(doctor.getId()))
////                return "Unauthorized";
////            t.setOrderIndex(index++);
////            tutorialRepository.save(t);
////        }
////        return "ok";
////    }
////
//////
//////    @PostMapping("/courses/{courseId}/tutorials")
//////    public String uploadTutorial(@PathVariable Long courseId,
//////                                 @RequestParam String title,
//////                                 @RequestParam TutorialType type,
//////                                 @RequestParam MultipartFile file,
//////                                 Principal principal,
//////                                 RedirectAttributes redirectAttributes) {
//////        try {
//////            User doctor = userRepository.findByUsername(principal.getName())
//////                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
//////            Course course = courseRepository.findByIdAndUser(courseId, doctor)
//////                    .orElseThrow(() -> new RuntimeException("Course not found"));
//////
//////            if (file.isEmpty()) {
//////                redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
//////                return "redirect:/dr/courses";
//////            }
//////
//////            if (!List.of("VIDEO", "PDF", "ARTICLE", "QUIZ").contains(type.name())) {
//////                redirectAttributes.addFlashAttribute("error", "Unsupported tutorial type.");
//////                return "redirect:/dr/courses";
//////            }
//////
//////            if (file.getSize() > 200 * 1024 * 1024) {
//////                redirectAttributes.addFlashAttribute("error", "File too large (max 200MB).");
//////                return "redirect:/dr/courses";
//////            }
//////
//////            String filePath = storageService.storeFile(file, doctor.getId(), courseId, type);
//////
//////            Tutorial tutorial = new Tutorial();
//////            tutorial.setTitle(title);
//////            tutorial.setType(type);
//////            tutorial.setFilePath(filePath);
//////            tutorial.setCourse(course);
////////            tutorial.setUser(doctor);
//////            tutorialRepository.save(tutorial);
//////
//////            redirectAttributes.addFlashAttribute("success", "Tutorial uploaded successfully.");
//////        } catch (Exception e) {
//////            redirectAttributes.addFlashAttribute("error", "Error uploading tutorial: " + e.getMessage());
//////        }
//////        return "redirect:/dr/courses";
//////    }
//////
//////    @PostMapping("/tutorials/edit/{id}")
//////    public String editTutorial(@PathVariable Long id,
//////                               @RequestParam String title,
//////                               @RequestParam TutorialType type,
//////                               @RequestParam(required = false) MultipartFile file,
//////                               Principal principal,
//////                               RedirectAttributes redirectAttributes) {
//////        try {
//////            Tutorial tutorial = tutorialRepository.findById(id)
//////                    .orElseThrow(() -> new RuntimeException("Tutorial not found"));
//////
//////            if (!tutorial.getUser().getUsername().equals(principal.getName())) {
//////                redirectAttributes.addFlashAttribute("error", "Unauthorized action.");
//////                return "redirect:/dr/courses";
//////            }
//////
//////            tutorial.setTitle(title);
//////            tutorial.setType(type);
//////
//////            if (file != null && !file.isEmpty()) {
//////                if (file.getSize() > 200 * 1024 * 1024) {
//////                    redirectAttributes.addFlashAttribute("error", "File too large (max 200MB).");
//////                    return "redirect:/dr/courses";
//////                }
//////                String filePath = fileStorageService.storeFile(file,
//////                        tutorial.getDoctor().getId(),
//////                        tutorial.getCourse().getId(),
//////                        type);
//////                tutorial.setFilePath(filePath);
//////            }
//////
//////            tutorialRepository.save(tutorial);
//////            redirectAttributes.addFlashAttribute("success", "Tutorial updated successfully.");
//////        } catch (Exception e) {
//////            redirectAttributes.addFlashAttribute("error", "Error updating tutorial: " + e.getMessage());
//////        }
//////        return "redirect:/dr/courses";
//////    }
//
////}
