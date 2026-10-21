package com.aminah.elearning.service;

import com.aminah.elearning.model.TutorialType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StorageServiceTest {
    @SuppressWarnings("unchecked")
    private ObjectProvider<PrivateObjectStorageService> provider(PrivateObjectStorageService remote) {
        ObjectProvider<PrivateObjectStorageService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(remote);
        return provider;
    }

    @Test
    void normalAuthoringUsesPrivateBackendAndStableProtectedPath() throws Exception {
        var remote = mock(PrivateObjectStorageService.class);
        var storage = new StorageService(provider(remote));
        var file = new MockMultipartFile("file", "clinical.pdf", "application/pdf", new byte[]{1, 2});
        String path = storage.storeFile(file, 2L, 24L, TutorialType.PDF);
        assertThat(path).startsWith("/uploads/2/24/pdfs/").endsWith("-clinical.pdf");
        verify(remote).store(path, file);
    }

    @Test
    void remoteFailureNeverSilentlyFallsBackToEphemeralDisk() throws Exception {
        var remote = mock(PrivateObjectStorageService.class);
        doThrow(new IllegalStateException("storage unavailable")).when(remote).store(anyString(), any());
        var storage = new StorageService(provider(remote));
        var file = new MockMultipartFile("file", "clinical.pdf", "application/pdf", new byte[]{1});
        assertThatThrownBy(() -> storage.storeFile(file, 2L, 24L, TutorialType.PDF))
                .isInstanceOf(IllegalStateException.class).hasMessage("storage unavailable");
    }

    @Test
    void rejectsInvalidUploadBeforeCallingPrivateBackend() throws Exception {
        var remote = mock(PrivateObjectStorageService.class);
        var storage = new StorageService(provider(remote));
        var file = new MockMultipartFile("file", "clinical.exe", "application/pdf", new byte[]{1});
        assertThatThrownBy(() -> storage.storeFile(file, 2L, 24L, TutorialType.PDF))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(remote);
    }
}
