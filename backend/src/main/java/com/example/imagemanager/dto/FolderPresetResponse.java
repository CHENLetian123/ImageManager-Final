package com.example.imagemanager.dto;

import com.example.imagemanager.entity.FolderPreset;

import java.time.LocalDateTime;

public class FolderPresetResponse {

    private Long id;
    private String name;
    private String sourcePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastSyncAt;

    public static FolderPresetResponse from(FolderPreset preset) {
        FolderPresetResponse response = new FolderPresetResponse();
        response.setId(preset.getId());
        response.setName(preset.getName());
        response.setSourcePath(preset.getSourcePath());
        response.setCreatedAt(preset.getCreatedAt());
        response.setUpdatedAt(preset.getUpdatedAt());
        response.setLastSyncAt(preset.getLastSyncAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
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

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }
}
