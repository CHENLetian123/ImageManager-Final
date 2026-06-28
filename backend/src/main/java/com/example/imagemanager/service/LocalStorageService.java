package com.example.imagemanager.service;

import com.example.imagemanager.dto.StorageResult;
import com.example.imagemanager.dto.StorageContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    @Value("${app.local.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.local.public-base-url:http://localhost:8080/local-files}")
    private String publicBaseUrl;

    @Override
    public String getName() {
        return "local";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public StorageResult upload(MultipartFile file, Long userId) {
        try {
            String extension = getExtension(file.getOriginalFilename());
            String objectKey = "users/" + userId + "/" + UUID.randomUUID() + extension;
            Path target = Path.of(uploadDir).resolve(objectKey).toAbsolutePath().normalize();
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StorageResult(objectKey, getPublicUrl(objectKey), normalizeContentType(file), file.getSize());
        } catch (IOException e) {
            throw new IllegalStateException("Local file upload failed.", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            Path target = Path.of(uploadDir).resolve(objectKey).toAbsolutePath().normalize();
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException("Local file delete failed.", e);
        }
    }

    @Override
    public StorageContent load(String objectKey, String fallbackContentType) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalStateException("Local object key is empty.");
        }
        try {
            Path target = Path.of(uploadDir).resolve(objectKey).toAbsolutePath().normalize();
            if (!Files.exists(target)) {
                throw new IllegalStateException("Local file does not exist.");
            }
            InputStream inputStream = Files.newInputStream(target);
            String contentType = Files.probeContentType(target);
            return new StorageContent(inputStream, normalizeContentType(contentType != null ? contentType : fallbackContentType), Files.size(target));
        } catch (IOException e) {
            throw new IllegalStateException("Local file read failed.", e);
        }
    }

    @Override
    public String getPublicUrl(String objectKey) {
        return trimEnd(publicBaseUrl, "/") + "/" + objectKey;
    }

    private String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        return index >= 0 ? fileName.substring(index).toLowerCase() : "";
    }

    private String normalizeContentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }

    private String normalizeContentType(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }

    private String trimEnd(String value, String suffix) {
        while (value.endsWith(suffix)) {
            value = value.substring(0, value.length() - suffix.length());
        }
        return value;
    }
}
