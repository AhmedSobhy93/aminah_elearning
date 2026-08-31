package com.aminah.elearning.config;

import com.aminah.elearning.service.RequestThrottleService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final RequestThrottleService throttle;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/profile/login".equals(request.getRequestURI())) {
            String remote = request.getRemoteAddr();
            String username = request.getParameter("username");
            boolean ipAllowed = throttle.allow("login-ip", remote, 40, Duration.ofMinutes(10));
            boolean accountAllowed = throttle.allow("login-account", username, 10, Duration.ofMinutes(10));
            if (!ipAllowed || !accountAllowed) {
                response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many login attempts. Please try again later.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
