package com.aminah.elearning.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService {

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

        String key = baseFolder + "/courses/" + courseId + "/pdf/"
                + UUID.randomUUID() + "-" + file.getOriginalFilename();

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
    public String generatePresignedVideoUploadUrl(Long courseId, String filename) {

        String key = baseFolder + "/courses/" + courseId + "/videos/" +
                UUID.randomUUID() + "-" + filename;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("video/mp4")
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(15))
                .build();

        return presigner.presignPutObject(presignRequest).url().toExternalForm();
    }

}
