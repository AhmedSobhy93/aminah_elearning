package com.aminah.elearning.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

/** Keeps tutorial paths stable while storing their bytes in a private S3-compatible bucket. */
@Service
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true")
public class PrivateObjectStorageService {
    private final S3Client client;
    private final S3Presigner presigner;
    private final String bucket;
    private final String prefix;

    public PrivateObjectStorageService(S3Client client, S3Presigner presigner,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.s3.folder:tutorials}") String prefix) {
        this.client = client;
        this.presigner = presigner;
        if (bucket.isBlank()) {
            throw new IllegalArgumentException("AWS_S3_BUCKET must be set for private storage");
        }
        if (!prefix.matches("[A-Za-z0-9_-]+(?:/[A-Za-z0-9_-]+)*")) {
            throw new IllegalArgumentException("AWS_S3_FOLDER must contain safe folder names");
        }
        this.bucket = bucket;
        this.prefix = prefix;
    }

    public void store(String filePath, MultipartFile file) throws IOException {
        var request = PutObjectRequest.builder().bucket(bucket).key(key(filePath))
                .contentType(file.getContentType()).cacheControl("private, no-store").build();
        try (var input = file.getInputStream()) {
            client.putObject(request, RequestBody.fromInputStream(input, file.getSize()));
        }
    }

    public URI downloadUrl(String filePath) {
        var object = GetObjectRequest.builder().bucket(bucket).key(key(filePath))
                .responseCacheControl("private, no-store").build();
        var request = GetObjectPresignRequest.builder().getObjectRequest(object)
                .signatureDuration(Duration.ofMinutes(5)).build();
        return URI.create(presigner.presignGetObject(request).url().toExternalForm());
    }

    private String key(String filePath) {
        if (filePath == null || !filePath.matches("/uploads/[0-9]+/[0-9]+/(?:videos|pdfs)/[A-Za-z0-9._-]+")
                || filePath.contains("..")) {
            throw new IllegalArgumentException("Invalid tutorial file path");
        }
        return prefix + filePath;
    }
}
