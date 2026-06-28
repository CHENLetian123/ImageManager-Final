package com.example.imagemanager.dto;

import com.example.imagemanager.entity.ImageItem;

public class FolderImageIndexResponse {

    private Long id;
    private String relativePath;
    private String originalPath;
    private Long fileSize;
    private Long lastModified;

    public static FolderImageIndexResponse from(ImageItem image) {
        FolderImageIndexResponse response = new FolderImageIndexResponse();
        response.setId(image.getId());
        response.setRelativePath(image.getRelativePath());
        response.setOriginalPath(image.getOriginalPath());
        response.setFileSize(image.getFileSize());
        response.setLastModified(image.getLastModified());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
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
}
