package com.aminah.elearning.config;

import com.aminah.elearning.service.RequestThrottleService;
import com.aminah.elearning.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RbacMatrixTest.ProbeController.class)
@Import({SecurityConfig.class, RbacMatrixTest.TestSecurityBeans.class, RbacMatrixTest.ProbeController.class})
class RbacMatrixTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Test
    void publicAndWebhookRoutesAreAnonymous() throws Exception {
        mvc.perform(get("/profile/login")).andExpect(status().isOk());
        mvc.perform(get("/profile/register")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());
        mvc.perform(post("/payments/webhook")).andExpect(status().isOk());
    }

    @Test
    void anonymousUsersCannotReachRoleOrFallbackRoutes() throws Exception {
        mvc.perform(get("/admin/probe")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/dr/probe")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/student/probe")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/authenticated-probe")).andExpect(status().is3xxRedirection());
    }

    @Test
    void studentCanUseOnlyStudentAndCheckoutRoutes() throws Exception {
        mvc.perform(get("/student/probe").with(user("student").roles("STUDENT"))).andExpect(status().isOk());
        mvc.perform(get("/payments/buy/1").with(user("student").roles("STUDENT"))).andExpect(status().isOk());
        mvc.perform(post("/payments/create/1").with(user("student").roles("STUDENT")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/admin/probe").with(user("student").roles("STUDENT"))).andExpect(status().isForbidden());
        mvc.perform(get("/users/probe").with(user("student").roles("STUDENT"))).andExpect(status().isForbidden());
        mvc.perform(get("/dr/probe").with(user("student").roles("STUDENT"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/upload/probe").with(user("student").roles("STUDENT")).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void doctorCanUseOnlyDoctorAndUploadRoutes() throws Exception {
        mvc.perform(get("/dr/probe").with(user("doctor").roles("DR"))).andExpect(status().isOk());
        mvc.perform(post("/api/upload/probe").with(user("doctor").roles("DR")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/admin/probe").with(user("doctor").roles("DR"))).andExpect(status().isForbidden());
        mvc.perform(get("/student/probe").with(user("doctor").roles("DR"))).andExpect(status().isForbidden());
        mvc.perform(get("/payments/buy/1").with(user("doctor").roles("DR"))).andExpect(status().isForbidden());
    }

    @Test
    void adminCanUseAdminAndUserManagementRoutesOnly() throws Exception {
        mvc.perform(get("/admin/probe").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(post("/users/probe").with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/dr/probe").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(get("/student/probe").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/upload/probe").with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void csrfProtectsAllOrdinaryMutations() throws Exception {
        mvc.perform(post("/users/probe").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/upload/probe").with(user("doctor").roles("DR"))).andExpect(status().isForbidden());
        mvc.perform(post("/payments/create/1").with(user("student").roles("STUDENT"))).andExpect(status().isForbidden());
    }

    @Test
    void fallbackRoutesRequireAnyAuthenticatedRole() throws Exception {
        mvc.perform(get("/authenticated-probe").with(user("student").roles("STUDENT"))).andExpect(status().isOk());
        mvc.perform(get("/authenticated-probe").with(user("doctor").roles("DR"))).andExpect(status().isOk());
        mvc.perform(get("/authenticated-probe").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
    }

    @RestController
    static class ProbeController {
        @GetMapping({"/profile/login", "/profile/register", "/actuator/health/readiness"})
        String publicGet() {
            return "ok";
        }

        @PostMapping("/payments/webhook")
        String webhook() {
            return "ok";
        }

        @GetMapping({"/admin/probe", "/users/probe", "/dr/probe", "/student/probe",
                "/payments/buy/1", "/authenticated-probe"})
        String protectedGet() {
            return "ok";
        }

        @PostMapping({"/users/probe", "/api/upload/probe", "/payments/create/1"})
        String protectedPost() {
            return "ok";
        }
    }

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        RequestThrottleService requestThrottleService() {
            return new RequestThrottleService();
        }

        @Bean
        LoginRateLimitFilter loginRateLimitFilter(RequestThrottleService throttle) {
            return new LoginRateLimitFilter(throttle);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return NoOpPasswordEncoder.getInstance();
        }
    }
}
