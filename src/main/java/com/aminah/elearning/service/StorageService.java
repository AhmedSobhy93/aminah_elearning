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

import static com.aminah.elearning.model.TutorialType.*;

@Service
public class StorageService {
    private final Path storageLocation = null;

   

//    public StorageService(FileStorageProperties  props) throws IOException {
//        this.storageLocation = Paths.get(props.getUploadDir()).toAbsolutePath().normalize();
//        Files.createDirectories(this.storageLocation);
//    }

    private final Path rootUpload = Paths.get("uploads");

    public StorageService() throws IOException {
        Files.createDirectories(rootUpload);
    }

    /**
     * Stores file organized by doctorId/courseId/type
     */
    public String storeFile(MultipartFile file, Long doctorId, Long courseId, TutorialType type) throws IOException {
        String folder = switch (type) {
            case VIDEO -> "videos";
            case PDF -> "pdfs";
            case ARTICLE -> "articles";
            case QUIZ -> "quizzes";
        };

        Path targetDir = rootUpload.resolve(String.valueOf(doctorId))
                .resolve(String.valueOf(courseId))
                .resolve(folder);

        Files.createDirectories(targetDir);

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path targetPath = targetDir.resolve(fileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return doctorId + "/" + courseId + "/" + folder + "/" + fileName; // Relative path stored in DB
    }

    public Path getFilePath(String relativePath) {
        return rootUpload.resolve(relativePath);
    }
//    public String store(MultipartFile file) throws IOException {
//        String filename = System.currentTimeMillis() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
//        Path target = this.storageLocation.resolve(filename);
//        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
//        return target.toString();
//    }

//    public Resource loadAsResource(String filepath) { ... }
}