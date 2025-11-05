package com.aminah.elearning.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class StorageService {
    private final Path storageLocation = null;

   

//    public StorageService(FileStorageProperties  props) throws IOException {
//        this.storageLocation = Paths.get(props.getUploadDir()).toAbsolutePath().normalize();
//        Files.createDirectories(this.storageLocation);
//    }

    public String store(MultipartFile file) throws IOException {
        String filename = System.currentTimeMillis() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
        Path target = this.storageLocation.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

//    public Resource loadAsResource(String filepath) { ... }
}