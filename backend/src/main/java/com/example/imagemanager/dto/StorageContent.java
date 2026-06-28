package com.example.imagemanager.dto;

import java.io.InputStream;

public class StorageContent {

    private final InputStream inputStream;
    private final String contentType;
    private final Long contentLength;

    public StorageContent(InputStream inputStream, String contentType, Long contentLength) {
        this.inputStream = inputStream;
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getContentLength() {
        return contentLength;
    }
}
