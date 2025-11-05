package com.aminah.elearning.service;

import com.aminah.elearning.model.PasswordResetToken;
import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.PasswordResetTokenRepository;
import com.aminah.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepo;
    @Autowired
    private EmailService emailService;

    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.userRepo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Not found"));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }

    public User userUpdate(User updatedUser) {
        User existingUser = userRepo.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

//        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        existingUser.setEmail(updatedUser.getEmail());
        return userRepo.save(existingUser);
    }


//    public void createPasswordResetToken(String email) {
//        User user = userRepo.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("No user found with email " + email));
//
//        String token = UUID.randomUUID().toString();
//        PasswordResetToken resetToken = new PasswordResetToken();
//        resetToken.setToken(token);
//        resetToken.setUser(user);
//        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
//        passwordResetTokenRepo.save(resetToken);
//
//        String link = "http://localhost:8080/reset-password?token=" + token;
//        emailService.sendVerificationEmail(user.getEmail(), "Password Reset Request",
//                "Click this link to reset your password: " + link);
//
//    }
//
//    public boolean resetPassword(String token, String newPassword) {
//        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token);
//        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
//            return false;
//        }
//
//        User user = resetToken.getUser();
//        user.setPassword(passwordEncoder.encode(newPassword));
//        userRepo.save(user);
//
//        passwordResetTokenRepo.delete(resetToken); // cleanup
//        return true;
//    }
}