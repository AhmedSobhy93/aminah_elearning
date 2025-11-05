package com.aminah.elearning;

import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
public class ElearningApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElearningApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner seed(UserRepository userRepository, PasswordEncoder encoder) {
//        return args -> {
//            if (userRepository.count() == 0) {
////                Set<Role> roles=new Role();
//                User admin = new User(); admin.setUsername("admin"); admin.setEmail("admin@aminah.com");
//                admin.setPassword(encoder.encode("AdminPass123!")); admin.setRole(Role.ADMIN
//                ); userRepository.save(admin);
//                User dr = new User(); dr.setUsername("drsamy"); dr.setEmail("dr.samy@aminah.com");
//                dr.setPassword(encoder.encode("DrPass123!")); dr.setRole(Role.DR); userRepository.save(dr);
//                User student = new User(); student.setUsername("student1"); student.setEmail("s1@aminah.com");
//                student.setPassword(encoder.encode("StudPass123!")); student.setRole(Role.STUDENT); userRepository.save(student);
//            }
//        };
//    }
}
