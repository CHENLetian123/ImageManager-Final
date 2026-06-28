package com.example.imagemanager.dto;

public class FolderSyncResultResponse {

    private int uploaded;
    private int updated;
    private int skipped;
    private int deleted;

    public FolderSyncResultResponse() {
    }

    public FolderSyncResultResponse(int uploaded, int updated, int skipped, int deleted) {
        this.uploaded = uploaded;
        this.updated = updated;
        this.skipped = skipped;
        this.deleted = deleted;
    }

    public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }
}
