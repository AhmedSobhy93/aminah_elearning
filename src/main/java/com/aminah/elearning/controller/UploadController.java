package com.aminah.elearning.controller;

import com.aminah.elearning.service.S3StorageService;
import com.aminah.elearning.service.DoctorContentAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true")
public class UploadController {

    private final S3StorageService storageService;
    private final DoctorContentAuthorizationService contentAuthorization;

    @PostMapping("/video-url")
    @PreAuthorize("hasRole('DR')")
    public ResponseEntity<Map<String,String>> getUploadUrl(
            @RequestParam Long courseId,
            @RequestParam String filename,
            @RequestParam Long contentLength,
            Principal principal) {

        contentAuthorization.requireOwnedCourse(courseId, principal.getName());
        String url = storageService.generatePresignedVideoUploadUrl(courseId, filename, contentLength);

        return ResponseEntity.ok(Map.of(
                "uploadUrl", url,
                "contentType", "video/mp4",
                "contentLength", contentLength.toString()
        ));
    }
}
