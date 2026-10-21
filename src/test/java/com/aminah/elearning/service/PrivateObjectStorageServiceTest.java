package com.aminah.elearning.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PrivateObjectStorageServiceTest {
    private static final String PATH = "/uploads/2/24/pdfs/uuid-clinical.pdf";

    @Test
    void storesValidatedPathWithoutRequestingPublicAccess() throws Exception {
        S3Client client = mock(S3Client.class);
        var storage = new PrivateObjectStorageService(client, mock(S3Presigner.class), "private-bucket", "tutorials");
        byte[] bytes = "%PDF-fixture".getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            PutObjectRequest request = invocation.getArgument(0);
            RequestBody body = invocation.getArgument(1);
            assertThat(request.bucket()).isEqualTo("private-bucket");
            assertThat(request.key()).isEqualTo("tutorials" + PATH);
            assertThat(request.aclAsString()).isNull();
            assertThat(request.cacheControl()).isEqualTo("private, no-store");
            try (var input = body.contentStreamProvider().newStream()) {
                assertThat(input.readAllBytes()).isEqualTo(bytes);
            }
            return null;
        }).when(client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        storage.store(PATH, new MockMultipartFile("file", "clinical.pdf", "application/pdf", bytes));
        verify(client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void signedDownloadUsesPrivateEndpointAndExpiresWithoutNetworkAccess() {
        try (var presigner = S3Presigner.builder().region(Region.of("auto"))
                .endpointOverride(URI.create("https://account.r2.cloudflarestorage.com"))
                .serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("qa-access", "qa-secret")))
                .build()) {
            var storage = new PrivateObjectStorageService(mock(S3Client.class), presigner, "private-bucket", "tutorials");
            URI link = storage.downloadUrl(PATH);
            assertThat(link.getHost()).endsWith("account.r2.cloudflarestorage.com");
            assertThat(link.getPath()).endsWith("tutorials" + PATH);
            assertThat(link.getQuery()).contains("X-Amz-Signature=", "response-cache-control=")
                    .containsPattern("X-Amz-Expires=(299|300)(?:&|$)");
        }
    }

    @Test
    void rejectsTraversalAndForeignUrlsBeforeAnyObjectRequest() {
        var client = mock(S3Client.class);
        var signer = mock(S3Presigner.class);
        var storage = new PrivateObjectStorageService(client, signer, "private-bucket", "tutorials");
        for (String path : new String[]{"/uploads/2/24/pdfs/../secret.pdf", "https://example.com/file", "/uploads/2/24/pdfs/a\\b.pdf"}) {
            assertThatThrownBy(() -> storage.downloadUrl(path)).isInstanceOf(IllegalArgumentException.class);
        }
        verifyNoInteractions(client, signer);
    }

    @Test
    void rejectsMissingBucketAndUnsafePrefix() {
        assertThatThrownBy(() -> new PrivateObjectStorageService(mock(S3Client.class), mock(S3Presigner.class), "", "tutorials"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PrivateObjectStorageService(mock(S3Client.class), mock(S3Presigner.class), "private", "../outside"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
