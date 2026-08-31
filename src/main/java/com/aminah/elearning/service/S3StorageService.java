package com.aminah.elearning.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true")
public class S3StorageService {
    private static final long MAX_PDF_SIZE_BYTES = 50L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE_BYTES = 500L * 1024 * 1024;

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.folder}")
    private String baseFolder;


    /* ------------------------------------------------------
       NORMAL PDF UPLOAD (S3Client.putObject)
       ------------------------------------------------------ */
    public String uploadPDF(MultipartFile file, Long courseId, String userId) throws IOException {
        validatePdf(file);

        String key = baseFolder + "/courses/" + courseId + "/pdf/"
                + buildSafeFilename(file.getOriginalFilename(), ".pdf");

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(req, RequestBody.fromBytes(file.getBytes()));

        return "https://" + bucket + ".s3.amazonaws.com/" + key;
    }


    /* ------------------------------------------------------
       PRESIGNED URL FOR VIDEO UPLOAD (AWS SDK v2)
       ------------------------------------------------------ */
    public String generatePresignedVideoUploadUrl(Long courseId, String filename, long contentLength) {
        if (contentLength <= 0 || contentLength > MAX_VIDEO_SIZE_BYTES) {
            throw new IllegalArgumentException("Video file is too large");
        }
        String safeFilename = buildSafeFilename(filename, ".mp4");

        String key = baseFolder + "/courses/" + courseId + "/videos/" +
                safeFilename;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("video/mp4")
                .contentLength(contentLength)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(15))
                .build();

        return presigner.presignPutObject(presignRequest).url().toExternalForm();
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }
        if (file.getSize() > MAX_PDF_SIZE_BYTES) {
            throw new IllegalArgumentException("PDF file is too large");
        }
        String contentType = file.getContentType();
        if (contentType == null || !"application/pdf".equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Invalid PDF content type");
        }
        buildSafeFilename(file.getOriginalFilename(), ".pdf");
    }

    private String buildSafeFilename(String originalFilename, String requiredExtension) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }
        String filename = org.springframework.util.StringUtils.cleanPath(originalFilename);
        filename = org.springframework.util.StringUtils.getFilename(filename);
        if (filename == null || filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(requiredExtension)) {
            throw new IllegalArgumentException("Invalid file extension");
        }
        String base = filename.substring(0, filename.length() - requiredExtension.length())
                .replaceAll("[^A-Za-z0-9._-]", "-");
        if (base.isBlank()) {
            base = "upload";
        }
        return UUID.randomUUID() + "-" + base + requiredExtension;
    }

}
