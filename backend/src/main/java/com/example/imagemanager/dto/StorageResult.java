package com.example.imagemanager.dto;

public class StorageResult {

    private String objectKey;
    private String publicUrl;
    private String contentType;
    private long fileSize;

    public StorageResult() {
    }

    public StorageResult(String objectKey, String publicUrl, String contentType, long fileSize) {
        this.objectKey = objectKey;
        this.publicUrl = publicUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
