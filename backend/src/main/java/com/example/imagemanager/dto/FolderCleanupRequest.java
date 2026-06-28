package com.example.imagemanager.dto;

import java.util.ArrayList;
import java.util.List;

public class FolderCleanupRequest {

    private boolean deleteMissing;
    private List<String> currentRelativePaths = new ArrayList<>();

    public boolean isDeleteMissing() {
        return deleteMissing;
    }

    public void setDeleteMissing(boolean deleteMissing) {
        this.deleteMissing = deleteMissing;
    }

    public List<String> getCurrentRelativePaths() {
        return currentRelativePaths;
    }

    public void setCurrentRelativePaths(List<String> currentRelativePaths) {
        this.currentRelativePaths = currentRelativePaths;
    }
}
