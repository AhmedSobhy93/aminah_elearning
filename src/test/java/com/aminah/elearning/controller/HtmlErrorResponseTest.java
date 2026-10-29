package com.aminah.elearning.controller;

import com.aminah.elearning.service.UserService;
import com.aminah.elearning.service.RequestThrottleService;
import com.aminah.elearning.utils.GlobalControllerAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HtmlErrorResponseTest.DeniedController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalControllerAdvice.class, HtmlErrorResponseTest.DeniedController.class, RequestThrottleService.class})
class HtmlErrorResponseTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private UserService users;

    @Test
    void browserDenialRendersExistingRecoveryPageWithoutInternalDetails() throws Exception {
        mvc.perform(get("/test-certificate-denied").accept(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Access unavailable")))
                .andExpect(content().string(containsString("Return home")))
                .andExpect(content().string(not(containsString("internal-enrollment-detail"))));
    }

    @Test
    void apiDenialKeepsTheProblemJsonContract() throws Exception {
        mvc.perform(get("/test-certificate-denied").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("internal-enrollment-detail"));
    }

    @RestController
    static class DeniedController {
        @GetMapping("/test-certificate-denied")
        void denied() {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "internal-enrollment-detail");
        }
    }
}
