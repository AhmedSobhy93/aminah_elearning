package com.aminah.elearning.service;

import com.aminah.elearning.model.TutorialType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StorageServiceTest {
    @org.junit.jupiter.api.io.TempDir java.nio.file.Path uploadRoot;
    @SuppressWarnings("unchecked")
    private ObjectProvider<PrivateObjectStorageService> provider(PrivateObjectStorageService remote) {
        ObjectProvider<PrivateObjectStorageService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(remote);
        return provider;
    }

    @Test
    void localUploadUsesConfiguredRootAndCanBeReadAfterServiceRestart() throws Exception {
        var storage = new StorageService(provider(null), uploadRoot.toString());
        byte[] bytes = "%PDF-1.4 local fixture".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var file = new MockMultipartFile("file", "clinical.pdf", "application/pdf", bytes);
        String protectedPath = storage.storeFile(file, 2L, 24L, TutorialType.PDF);
        assertThat(protectedPath).startsWith("/uploads/2/24/pdfs/");
        java.nio.file.Path physical = uploadRoot.resolve(protectedPath.substring("/uploads/".length()));
        assertThat(java.nio.file.Files.readAllBytes(physical)).isEqualTo(bytes);
        var restarted = new StorageService(provider(null), uploadRoot.toString());
        assertThat(restarted.getFilePath(protectedPath)).isEqualTo(physical.toRealPath());
        assertThat(java.nio.file.Files.readAllBytes(restarted.getFilePath(protectedPath))).isEqualTo(bytes);
    }

    @Test
    void configuredRootCannotBeEscapedThroughProtectedFilePath() throws Exception {
        var storage = new StorageService(provider(null), uploadRoot.toString());
        assertThatThrownBy(() -> storage.getFilePath("/uploads/../../outside.pdf")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankConfiguredRootIsRejected() {
        assertThatThrownBy(() -> new StorageService(provider(null), " ")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void normalAuthoringUsesPrivateBackendAndStableProtectedPath() throws Exception {
        var remote = mock(PrivateObjectStorageService.class);
        var storage = new StorageService(provider(remote), uploadRoot.toString());
        var file = new MockMultipartFile("file", "clinical.pdf", "application/pdf", new byte[]{1, 2});
        String path = storage.storeFile(file, 2L, 24L, TutorialType.PDF);
        assertThat(path).startsWith("/uploads/2/24/pdfs/").endsWith("-clinical.pdf");
        verify(remote).store(path, file);
    }

    @Test
    void remoteFailureNeverSilentlyFallsBackToEphemeralDisk() throws Exception {
        var remote = mock(PrivateObjectStorageService.class);
        doThrow(new IllegalStateException("storage unavailable")).when(remote).store(anyString(), any());
        var storage = new StorageService(provider(remote), uploadRoot.toString());
        var file = new MockMultipartFile("file", "clinical.pdf", "application/pdf", new byte[]{1});
        assertThatThrownBy(() -> storage.storeFile(file, 2L, 24L, TutorialType.PDF))
                .isInstanceOf(IllegalStateException.class).hasMessage("storage unavailable");
    }

    @Test
    void rejectsInvalidUploadBeforeCallingPrivateBackend() throws Exception {
        var remote = mock(PrivateObjectStorageService.class);
        var storage = new StorageService(provider(remote), uploadRoot.toString());
        var file = new MockMultipartFile("file", "clinical.exe", "application/pdf", new byte[]{1});
        assertThatThrownBy(() -> storage.storeFile(file, 2L, 24L, TutorialType.PDF))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(remote);
    }
}
