package com.aminah.elearning;

import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//import static jdk.internal.org.jline.reader.impl.LineReaderImpl.CompletionType.List;

@SpringBootApplication
public class ElearningApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElearningApplication.class, args);
    }
    @Bean
    public CommandLineRunner seedData(
            UserRepository userRepository,
            CourseRepository courseRepository,
            SectionRepository sectionRepository,
            TutorialRepository tutorialRepository,
            CourseEnrollmentRepository enrollmentRepository,
            PasswordEncoder encoder
    ) {
        return args -> {

            if (userRepository.count() > 3) {
                System.out.println("Seed already exists — skipping.");
                return;
            }

            // -----------------------
            // USERS
            // -----------------------
            // -----------------------
// USERS
// -----------------------
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@aminah.com");
            admin.setPassword(encoder.encode("P@ssw0rd"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            User dr = new User();
            dr.setUsername("drsaber");
            dr.setEmail("dr.saber@aminah.com");
            dr.setPassword(encoder.encode("P@ssw0rd"));
            dr.setRole(Role.DR);
            userRepository.save(dr);

            User student1 = new User();
            student1.setUsername("student1");
            student1.setEmail("s1@aminah.com");
            student1.setPassword(encoder.encode("P@ssw0rd"));
            student1.setRole(Role.STUDENT);
            userRepository.save(student1);

            User student2 = new User();
            student2.setUsername("student2");
            student2.setEmail("s2@aminah.com");
            student2.setPassword(encoder.encode("P@ssw0rd"));
            student2.setRole(Role.STUDENT);
            userRepository.save(student2);

            User student3 = new User();
            student3.setUsername("student3");
            student3.setEmail("s3@aminah.com");
            student3.setPassword(encoder.encode("P@ssw0rd"));
            student3.setRole(Role.STUDENT);
            userRepository.save(student3);

            List<User> students = List.of(student1, student2, student3);

            // -----------------------
            // COURSE GENERATOR LOGIC
            // -----------------------

            // Each course will have a different number of sections
            int[] sectionsPerCourse = {3, 2, 4, 5, 3, 6};

            // Different tutorials count per section (looped)
            int[] tutorialsPerSectionPattern = {2, 3, 1, 4}; // rotates

            // Tutorial types rotation
            TutorialType[] tutorialTypes = {
                    TutorialType.PDF,
                    TutorialType.VIDEO,
                    TutorialType.ARTICLE
            };

            for (int c = 1; c <= 6; c++) {

                Course course = new Course();
                course.setAuthor(dr);
                course.setTitle("Course Title " + c);
                course.setCourseName("Course " + c);
                course.setDescription("This is a unique description for Course #" + c);
                course.setPrice(200.0 + (c * 40));
                courseRepository.save(course);

                int numberOfSections = sectionsPerCourse[c - 1];

                // -------------------------------------
                // GENERATE SECTIONS FOR THIS COURSE
                // -------------------------------------
                for (int s = 1; s <= numberOfSections; s++) {

                    Section section = new Section();
                    section.setCourse(course);
                    section.setTitle("Section " + s + " of Course " + c);
                    section.setDescription("Details about section " + s + " of course " + c);
                    section.setOrderIndex(s);
                    sectionRepository.save(section);

                    // number of tutorials for this section (rotating)
                    int tutorialCount = tutorialsPerSectionPattern[(s - 1) % tutorialsPerSectionPattern.length];

                    // -------------------------------------
                    // GENERATE TUTORIALS FOR THIS SECTION
                    // -------------------------------------
                    for (int t = 1; t <= tutorialCount; t++) {

                        Tutorial tutorial = new Tutorial();
                        tutorial.setSection(section);
                        tutorial.setCourse(course);
                        tutorial.setUser(dr);
                        tutorial.setTitle("Tutorial " + t + " (Section " + s + ", Course " + c + ")");
                        tutorial.setOrderIndex(t);

                        // rotate tutorial type
                        TutorialType type = tutorialTypes[(t - 1) % tutorialTypes.length];
                        tutorial.setType(type);

                        switch (type) {
                            case PDF -> tutorial.setFilePath("/samples/course" + c + "/section" + s + "/tutorial" + t + ".pdf");
                            case VIDEO -> tutorial.setFilePath("/videos/sample" + t + ".mp4");
                            case ARTICLE -> tutorial.setArticleContent("This is a sample article for tutorial " + t);
                        }

                        tutorialRepository.save(tutorial);
                    }
                }

                // -----------------------
                // ENROLL ALL STUDENTS
                // -----------------------
                for (User student : students) {
                    CourseEnrollment enrollment = new CourseEnrollment();
                    enrollment.setCourse(course);
                    enrollment.setUser(student);
                    enrollment.setPaymentStatus("SUCCESS");
                    enrollment.setEnrollmentDate(LocalDateTime.now());
                    enrollmentRepository.save(enrollment);
                }
            }

            System.out.println("Seed completed:");
            System.out.println("Users: " + userRepository.count());
            System.out.println("Courses: " + courseRepository.count());
            System.out.println("Sections: " + sectionRepository.count());
            System.out.println("Tutorials: " + tutorialRepository.count());
            System.out.println("Enrollments: " + enrollmentRepository.count());
        };
    }

}
