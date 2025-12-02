package com.aminah.elearning.controller;

import com.aminah.elearning.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final S3StorageService storageService;

    @GetMapping("/video-url")
    public ResponseEntity<Map<String,String>> getUploadUrl(
            @RequestParam Long courseId,
            @RequestParam String filename) {

        String url = storageService.generatePresignedVideoUploadUrl(courseId, filename);

        return ResponseEntity.ok(Map.of("uploadUrl", url));
    }
}

