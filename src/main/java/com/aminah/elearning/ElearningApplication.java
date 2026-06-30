package com.aminah.elearning;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.CourseLevel;
import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.Section;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.TutorialType;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.SectionRepository;
import com.aminah.elearning.repository.TutorialRepository;
import com.aminah.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class ElearningApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElearningApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
    public CommandLineRunner seedData(
            UserRepository userRepository,
            CourseRepository courseRepository,
            SectionRepository sectionRepository,
            TutorialRepository tutorialRepository,
            CourseEnrollmentRepository enrollmentRepository,
            PasswordEncoder encoder,
            @Value("${app.seed.default-password}") String seedPassword
    ) {
        return args -> {
            if (seedPassword == null || seedPassword.isBlank()) {
                throw new IllegalStateException("APP_SEED_DEFAULT_PASSWORD must be set when APP_SEED_ENABLED=true");
            }

            User admin = getOrCreateUser(userRepository, encoder, seedPassword,
                    "admin", "admin@aminah.com", "Admin User", null, Role.ADMIN);
            User dr = getOrCreateUser(userRepository, encoder, seedPassword,
                    "drsaber", "dr.saber@aminah.com", "Dr. Saber", "123456789", Role.DR);
            User student1 = getOrCreateUser(userRepository, encoder, seedPassword,
                    "student1", "s1@aminah.com", "Student One", null, Role.STUDENT);
            User student2 = getOrCreateUser(userRepository, encoder, seedPassword,
                    "student2", "s2@aminah.com", "Student Two", null, Role.STUDENT);
            User student3 = getOrCreateUser(userRepository, encoder, seedPassword,
                    "student3", "s3@aminah.com", "Student Three", null, Role.STUDENT);

            List<User> students = List.of(student1, student2, student3);

            if (courseRepository.count() > 0) {
                System.out.println("Demo courses already exist - skipping course seed.");
                return;
            }

            int[] sectionsPerCourse = {3, 2, 4, 5, 3, 6};
            int[] tutorialsPerSectionPattern = {2, 3, 1, 4};
            TutorialType[] tutorialTypes = {
                    TutorialType.PDF,
                    TutorialType.VIDEO,
                    TutorialType.ARTICLE
            };

            for (int c = 1; c <= 6; c++) {
                Course course = new Course();
                course.setAuthor(dr);
                course.setTitle("Course Title " + c);
                course.setLevel(CourseLevel.BEGINNER);
                course.setCourseName("Course " + c);
                course.setDescription("This is a unique description for Course #" + c);
                course.setPrice(200.0 + (c * 40));
                courseRepository.save(course);

                int numberOfSections = sectionsPerCourse[c - 1];

                for (int s = 1; s <= numberOfSections; s++) {
                    Section section = new Section();
                    section.setCourse(course);
                    section.setTitle("Section " + s + " of Course " + c);
                    section.setDescription("Details about section " + s + " of course " + c);
                    section.setOrderIndex(s);
                    sectionRepository.save(section);

                    int tutorialCount = tutorialsPerSectionPattern[(s - 1) % tutorialsPerSectionPattern.length];

                    for (int t = 1; t <= tutorialCount; t++) {
                        Tutorial tutorial = new Tutorial();
                        tutorial.setSection(section);
                        tutorial.setUser(dr);
                        tutorial.setTitle("Tutorial " + t + " (Section " + s + ", Course " + c + ")");
                        tutorial.setOrderIndex(t);

                        TutorialType type = tutorialTypes[(t - 1) % tutorialTypes.length];
                        tutorial.setType(type);

                        switch (type) {
                            case PDF -> tutorial.setFilePath("/samples/course" + c + "/section" + s + "/tutorial" + t + ".pdf");
                            case VIDEO -> tutorial.setFilePath("/videos/sample" + t + ".mp4");
                            case ARTICLE -> tutorial.setArticleContent("This is a sample article for tutorial " + t);
                            case QUIZ -> {
                            }
                        }

                        tutorialRepository.save(tutorial);
                    }
                }

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

    private static User getOrCreateUser(
            UserRepository userRepository,
            PasswordEncoder encoder,
            String seedPassword,
            String username,
            String email,
            String fullName,
            String phoneNumber,
            Role role
    ) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setFullName(fullName);
                    user.setPhoneNumber(phoneNumber);
                    user.setPassword(encoder.encode(seedPassword));
                    user.setRole(role);
                    user.setEnabled(true);
                    return userRepository.save(user);
                });
    }
}
