package com.example.EcoSwap.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB

    private final String uploadDir = "uploads";

    public FileUploadService() {
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isImageValid(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        if (file.getSize() > MAX_IMAGE_SIZE) return false;
        String contentType = file.getContentType();
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        if (!isImageValid(file)) return null;
        return uploadFile(file);
    }

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir, newFilename);
            Files.copy(file.getInputStream(), filePath);

            return "/" + uploadDir + "/" + newFilename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;
        try {
            String relativePath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
            Path path = Paths.get(relativePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
