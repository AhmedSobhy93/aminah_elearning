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
    public CommandLineRunner seed(UserRepository userRepository, CourseRepository courseRepository, TutorialRepository tutorialRepository, CourseEnrollmentRepository courseEnrollmentRepository, PasswordEncoder encoder, SectionRepository sectionRepository) {
        return args -> {
            if (userRepository.count() == 0) {

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
                User student = new User();
                student.setUsername("student1");
                student.setEmail("s1@aminah.com");
                student.setPassword(encoder.encode("P@ssw0rd"));
                student.setRole(Role.STUDENT);
                userRepository.save(student);
                Course course = new Course();
                course.setCourseName("Course 1");
                course.setAuthor(dr);
                course.setTitle("Course 1");
                course.setDescription("Course Description");
                course.setCourseName("Namee");
                course.setPrice(Double.valueOf(234));

                courseRepository.save(course);
                Section section = new Section();
                section.setCourse(course);
                section.setTitle("Section 1");
                section.setOrderIndex(1);
                sectionRepository.save(section);
                List<Section> sectionList = new ArrayList<>();
                sectionList.add(section);
                course.setSections(sectionList);
                Tutorial tutorial = new Tutorial();
                tutorial.setSection(section);
                tutorial.setTitle("Tutorial title");
                tutorial.setType(TutorialType.PDF);
                tutorial.setFilePath("Tutorial file path");
                tutorial.setOrderIndex(2);
                Tutorial tutorial2 = new Tutorial();
                tutorial2.setSection(section);
                tutorial2.setTitle("Tutorial title");
                tutorial2.setType(TutorialType.PDF);
                tutorial2.setFilePath("Tutorial file path");
                tutorial2.setOrderIndex(1);
                List<Tutorial> tutorialList = new ArrayList<>();
                tutorialList.add(tutorial);
                tutorialList.add(tutorial2);

                section.setTutorials(tutorialList);

                tutorialRepository.save(tutorial);
                tutorialRepository.save(tutorial2);
                CourseEnrollment courseEnrollment = new CourseEnrollment();
                courseEnrollment.setCourse(course);
                courseEnrollment.setUser(student);
                courseEnrollment.setEnrollmentDate(LocalDateTime.MAX);
                courseEnrollmentRepository.save(courseEnrollment);

            }
        };
    }
}
