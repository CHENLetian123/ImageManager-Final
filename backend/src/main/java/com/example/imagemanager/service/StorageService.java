package com.example.imagemanager.service;

import com.example.imagemanager.dto.StorageResult;
import com.example.imagemanager.dto.StorageContent;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String getName();

    boolean isAvailable();

    StorageResult upload(MultipartFile file, Long userId);

    void delete(String objectKey);

    StorageContent load(String objectKey, String fallbackContentType);

    String getPublicUrl(String objectKey);
}
