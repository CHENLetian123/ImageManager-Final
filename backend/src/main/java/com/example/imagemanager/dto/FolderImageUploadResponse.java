package com.example.imagemanager.dto;

public class FolderImageUploadResponse {

    private String status;
    private ImageResponse image;

    public FolderImageUploadResponse(String status, ImageResponse image) {
        this.status = status;
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ImageResponse getImage() {
        return image;
    }

    public void setImage(ImageResponse image) {
        this.image = image;
    }
}
