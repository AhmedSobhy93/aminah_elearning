package com.aminah.elearning.service;

import com.aminah.elearning.utils.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.base-url:}") // optional base URL if you use CDN
    private String baseUrl;

    @Override
    public String storeFile(MultipartFile file, String userId, String courseId, Object type) throws IOException {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) ext = original.substring(original.lastIndexOf('.'));
        String key = String.format("users/%s/courses/%s/%s-%s%s", userId, courseId, Instant.now().getEpochSecond(), UUID.randomUUID(), ext);

        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(por, RequestBody.fromBytes(file.getBytes()));

        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl + "/" + key;
        } else {
            // Construct S3 URL (region-specific may be needed)
            // If using virtual-hosted-style: https://{bucket}.s3.{region}.amazonaws.com/{key}
            return String.format("https://%s.s3.amazonaws.com/%s", bucket, key);
        }
    }

    @Override
    public void delete(String fileUrl) throws Exception {
        // naive: try to extract key from URL after bucket/
        if (fileUrl == null) return;
        String key;
        if (fileUrl.contains(bucket + "/")) {
            key = fileUrl.substring(fileUrl.indexOf(bucket + "/") + (bucket + "/").length());
        } else {
            // if URL ends with key
            URI uri = new URI(fileUrl);
            key = uri.getPath();
            if (key.startsWith("/")) key = key.substring(1);
        }
        DeleteObjectRequest dor = DeleteObjectRequest.builder().bucket(bucket).key(key).build();
        s3Client.deleteObject(dor);
    }
}
