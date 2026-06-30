package com.aminah.elearning.service;

import com.aminah.elearning.model.TutorialType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {
    private static final long MAX_VIDEO_SIZE_BYTES = 500L * 1024 * 1024;
    private static final long MAX_PDF_SIZE_BYTES = 50L * 1024 * 1024;
    private static final Set<String> PDF_CONTENT_TYPES = Set.of("application/pdf");
    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of("video/mp4", "application/mp4");

    private final Path rootUpload = Paths.get("uploads");

    public StorageService() throws IOException {
        Files.createDirectories(rootUpload);
    }

    public String storeFile(MultipartFile file, Long doctorId, Long courseId, TutorialType type) throws IOException {
        validateFile(file, type);

        String folder = switch (type) {
            case VIDEO -> "videos";
            case PDF -> "pdfs";
            case ARTICLE -> "articles";
            case QUIZ -> "quizzes";
        };

        Path targetDir = rootUpload.resolve(String.valueOf(doctorId))
                .resolve(String.valueOf(courseId))
                .resolve(folder)
                .normalize();

        Files.createDirectories(targetDir);

        String fileName = buildSafeFileName(file.getOriginalFilename(), type);
        Path targetPath = targetDir.resolve(fileName).normalize();
        if (!targetPath.startsWith(targetDir)) {
            throw new IllegalArgumentException("Invalid upload path");
        }

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return "/" + rootUpload + "/" + doctorId + "/" + courseId + "/" + folder + "/" + fileName;
    }

    public Path getFilePath(String relativePath) throws IOException {
        String normalized = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        String uploadPrefix = rootUpload + "/";
        if (normalized.startsWith(uploadPrefix)) {
            normalized = normalized.substring(uploadPrefix.length());
        }

        Path resolved = rootUpload.resolve(normalized).normalize();
        Path root = rootUpload.toRealPath();
        Path absoluteResolved = resolved.toAbsolutePath().normalize();
        if (!absoluteResolved.startsWith(root.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return resolved;
    }

    private void validateFile(MultipartFile file, TutorialType type) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Upload file is required");
        }
        if (type != TutorialType.VIDEO && type != TutorialType.PDF) {
            throw new IllegalArgumentException("Only video and PDF tutorial files can be uploaded");
        }

        String extension = getExtension(file.getOriginalFilename());
        String contentType = file.getContentType();
        if (type == TutorialType.VIDEO) {
            validateType(extension, contentType, ".mp4", VIDEO_CONTENT_TYPES);
            validateSize(file.getSize(), MAX_VIDEO_SIZE_BYTES, "Video");
        } else {
            validateType(extension, contentType, ".pdf", PDF_CONTENT_TYPES);
            validateSize(file.getSize(), MAX_PDF_SIZE_BYTES, "PDF");
        }
    }

    private void validateType(String extension, String contentType, String expectedExtension, Set<String> contentTypes) {
        if (!expectedExtension.equals(extension) || contentType == null || !contentTypes.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid file type");
        }
    }

    private void validateSize(long size, long maxBytes, String label) {
        if (size > maxBytes) {
            throw new IllegalArgumentException(label + " file is too large");
        }
    }

    private String buildSafeFileName(String originalFilename, TutorialType type) {
        String extension = getExtension(originalFilename);
        String baseName = StringUtils.getFilename(StringUtils.cleanPath(originalFilename == null ? "" : originalFilename));
        if (baseName == null || baseName.isBlank() || baseName.contains("..") || baseName.contains("/") || baseName.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        String normalizedBase = baseName.substring(0, baseName.length() - extension.length())
                .replaceAll("[^A-Za-z0-9._-]", "-");
        if (normalizedBase.isBlank()) {
            normalizedBase = type.name().toLowerCase(Locale.ROOT);
        }
        return UUID.randomUUID() + "-" + normalizedBase + extension;
    }

    private String getExtension(String originalFilename) {
        String filename = StringUtils.getFilename(StringUtils.cleanPath(originalFilename == null ? "" : originalFilename));
        if (filename == null || filename.isBlank() || filename.contains("..")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            throw new IllegalArgumentException("Missing file extension");
        }
        return filename.substring(dot).toLowerCase(Locale.ROOT);
    }
}
