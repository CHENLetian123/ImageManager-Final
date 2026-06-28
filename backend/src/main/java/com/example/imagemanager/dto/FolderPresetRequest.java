package com.example.imagemanager.dto;

import jakarta.validation.constraints.NotBlank;

public class FolderPresetRequest {

    @NotBlank
    private String name;

    private String sourcePath;

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
}
