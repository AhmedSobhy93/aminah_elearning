package com.aminah.elearning.controller;

import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.TutorialRepository;
import com.aminah.elearning.service.CourseEnrollmentService;
import com.aminah.elearning.service.StorageService;
import com.aminah.elearning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Path;

@Controller
@RequiredArgsConstructor
public class TutorialFileController {

    private final StorageService storageService;
    private final TutorialRepository tutorialRepository;
    private final UserService userService;
    private final CourseEnrollmentService enrollmentService;

    @GetMapping("/uploads/{doctorId}/{sectionId}/{folder}/{filename:.+}")
    public ResponseEntity<Resource> serveTutorialFile(
            @PathVariable String doctorId,
            @PathVariable String sectionId,
            @PathVariable String folder,
            @PathVariable String filename,
            Authentication authentication
    ) throws IOException {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String filePath = "/uploads/" + doctorId + "/" + sectionId + "/" + folder + "/" + filename;
        Tutorial tutorial = tutorialRepository.findByFilePath(filePath).orElse(null);
        if (tutorial == null || !canAccess(authentication.getName(), tutorial)) {
            return ResponseEntity.status(403).build();
        }

        Path path = storageService.getFilePath(filePath);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    private boolean canAccess(String username, Tutorial tutorial) {
        User user = userService.findByUsername(username);
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (user.getRole() == Role.DR) {
            return ownsTutorial(user, tutorial);
        }
        if (tutorial.isPreview()) {
            return true;
        }
        return enrollmentService.hasAccess(user, tutorial.getSection().getCourse().getId());
    }

    private boolean ownsTutorial(User user, Tutorial tutorial) {
        if (tutorial.getUser() != null && user.getId().equals(tutorial.getUser().getId())) {
            return true;
        }
        return tutorial.getSection() != null
                && tutorial.getSection().getCourse() != null
                && tutorial.getSection().getCourse().getAuthor() != null
                && user.getId().equals(tutorial.getSection().getCourse().getAuthor().getId());
    }
}
