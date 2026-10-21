package com.aminah.elearning.controller;

import com.aminah.elearning.service.StudentCertificateService;
import com.aminah.elearning.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/student/certificates")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentCertificateController {
    private final StudentCertificateService certificates;
    private final UserService users;

    @GetMapping("/{courseId}")
    public String certificate(@PathVariable Long courseId, Authentication authentication, Model model) {
        model.addAttribute("certificate", certificates.getCertificate(userId(authentication), courseId));
        return "student/certificate";
    }

    @PostMapping("/{courseId}/issue")
    public String issue(@PathVariable Long courseId, Authentication authentication) {
        Long userId = userId(authentication);
        certificates.generateCertificate(userId, courseId);
        return "redirect:/student/certificates/" + courseId;
    }

    @GetMapping("/{courseId}/download")
    public String download(@PathVariable Long courseId, Authentication authentication,
                           Model model, HttpServletResponse response) {
        var certificate = certificates.getCertificate(userId(authentication), courseId);
        if (!certificate.issued()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Issue the certificate before downloading");
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + certificate.number() + ".html\"");
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("certificate", certificate);
        model.addAttribute("download", true);
        return "student/certificate";
    }

    private Long userId(Authentication authentication) {
        return users.findByUsername(authentication.getName()).getId();
    }
}
