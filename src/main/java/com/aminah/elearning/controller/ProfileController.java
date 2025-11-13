package com.aminah.elearning.controller;

import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.User;
import com.aminah.elearning.model.VerificationToken;
import com.aminah.elearning.repository.PasswordResetTokenRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.repository.VerificationTokenRepository;
import com.aminah.elearning.service.RegistrationService;
import com.aminah.elearning.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationService registrationService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserService userService;
    @Value("${app.url:http://localhost:8081}")
    private String appUrl;
    private final AuthenticationManager authenticationManager;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder, RegistrationService registrationService, VerificationTokenRepository verificationTokenRepository, UserService userService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.registrationService=registrationService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.userService = userService;


        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/profile")
    private String getProfile( Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();


        if (auth == null) {
            // no user logged in
            model.addAttribute("error", "Please Login in first");
            return "redirect:/";
        }

        String username = auth.getName(); // or ((UserDetails)auth.getPrincipal()).getUsername()

        // Fetch your app's User entity
        Optional<User> user = userRepository.findByUsername(username);
        if (user == null) {
            // This means the logged-in user isn’t in DB
            model.addAttribute("error", "User not found in the database");
            return "/profile/login";
        }
        model.addAttribute("user", user != null ? user : new User());
        return "/profile/profile";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "profile/register";
    }
    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        registrationService.register(user, appUrl);
        model.addAttribute("message", "Check your email for confirmation link!");
        return "profile/login";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute User user, Model model) {
//        Optional<User> updatedUser=userRepository.findById(user.getId());

        userService.userUpdate(user);
        model.addAttribute("message", "Profile Updated Successfully!");
        return "profile/profile";
    }

    @GetMapping("/confirm")
    public String confirmAccount(@RequestParam("token") String token, Model model) {
//        boolean verified = registrationService.confirmToken(token);
//        model.addAttribute("message", verified ? "Account verified! You can login now." : "Invalid or expired token.");

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            model.addAttribute("error", "Verification link expired.");
            return "error/expired";
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        model.addAttribute("message", "Account verified successfully!");
        return "profile/login";
    }
//    @PostMapping("/register")
//    public String processRegistration(@ModelAttribute User user) {
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setRole(Role.STUDENT); // default role
//        userRepository.save(user);
//        return "redirect:/";
//    }

    @GetMapping("/login") public String login(){
        return "profile/login";
    }

    // Handle login form POST
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password, HttpServletRequest request,
                               Model model) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Set authenticated user in session
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", authentication);

            return "redirect:/";

        } catch (BadCredentialsException e) {
            model.addAttribute("error", "Invalid username or password");
        } catch (DisabledException e) {
            model.addAttribute("error", "Your account is disabled. Please contact admin.");
        } catch (AuthenticationException e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
        }
           return "profile/login";

    }

    // Optional: logout
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }
}
