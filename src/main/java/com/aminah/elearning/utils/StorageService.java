package com.aminah.elearning.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    /**
     * Store file and return the public URL or path.
     */
    String storeFile(MultipartFile file, String userId, String courseId, Object type) throws IOException;
    void delete(String fileUrl) throws Exception;
}
