package com.example.imagemanager.dto;

import jakarta.validation.constraints.NotBlank;

public class TagRequest {

    @NotBlank
    private String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
