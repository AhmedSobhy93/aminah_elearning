package com.aminah.elearning.controller;

import com.aminah.elearning.config.LoginRateLimitFilter;
import com.aminah.elearning.config.SecurityConfig;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.repository.VerificationTokenRepository;
import com.aminah.elearning.service.RegistrationService;
import com.aminah.elearning.service.RequestThrottleService;
import com.aminah.elearning.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, LoginRateLimitFilter.class, RequestThrottleService.class})
class LoginFeedbackTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private UserRepository users;
    @MockitoBean private VerificationTokenRepository verificationTokens;
    @MockitoBean private RegistrationService registrations;
    @MockitoBean private UserService userService;
    @MockitoBean private PasswordEncoder passwordEncoder;

    @Test
    void failedLoginRedirectRendersGenericAccessibleRetryFeedback() throws Exception {
        mvc.perform(get("/profile/login").param("error", "untrusted-detail"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("role=\"alert\"")))
                .andExpect(content().string(containsString("Sign-in failed. Check your username and password and try again.")))
                .andExpect(content().string(not(containsString("untrusted-detail"))));
    }

    @Test
    void logoutRedirectRendersSuccessFeedbackAndNormalLoginHasNoFailure() throws Exception {
        mvc.perform(get("/profile/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You have been signed out successfully.")));
        mvc.perform(get("/profile/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Sign-in failed."))))
                .andExpect(content().string(not(containsString("You have been signed out successfully."))));
    }
}
