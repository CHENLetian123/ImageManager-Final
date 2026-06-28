package com.example.imagemanager.dto;

import com.example.imagemanager.entity.ImageItem;

import java.time.LocalDateTime;

public class ImageResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String tags;
    private String sourceType;
    private String sourceName;
    private String originalFileName;
    private String originalPath;
    private String relativePath;
    private String r2ObjectKey;
    private String publicUrl;
    private String contentType;
    private Long fileSize;
    private Long lastModified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ImageResponse from(ImageItem image) {
        ImageResponse response = new ImageResponse();
        response.setId(image.getId());
        response.setTitle(image.getTitle());
        response.setDescription(image.getDescription());
        response.setCategory(image.getCategory());
        response.setTags(image.getTags());
        response.setSourceType(image.getSourceType());
        response.setSourceName(image.getSourceName());
        response.setOriginalFileName(image.getOriginalFileName());
        response.setOriginalPath(image.getOriginalPath());
        response.setRelativePath(image.getRelativePath());
        response.setR2ObjectKey(image.getR2ObjectKey());
        response.setPublicUrl(image.getPublicUrl());
        response.setContentType(image.getContentType());
        response.setFileSize(image.getFileSize());
        response.setLastModified(image.getLastModified());
        response.setCreatedAt(image.getCreatedAt());
        response.setUpdatedAt(image.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getR2ObjectKey() {
        return r2ObjectKey;
    }

    public void setR2ObjectKey(String r2ObjectKey) {
        this.r2ObjectKey = r2ObjectKey;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
