package com.example.imagemanager.controller;

import com.example.imagemanager.dto.FolderCleanupRequest;
import com.example.imagemanager.dto.FolderImageIndexResponse;
import com.example.imagemanager.dto.FolderImageUploadResponse;
import com.example.imagemanager.dto.FolderPresetRequest;
import com.example.imagemanager.dto.FolderPresetResponse;
import com.example.imagemanager.dto.FolderSyncResultResponse;
import com.example.imagemanager.dto.MessageResponse;
import com.example.imagemanager.entity.User;
import com.example.imagemanager.service.FolderPresetService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/folder-presets")
public class FolderPresetController {

    private final FolderPresetService folderPresetService;

    public FolderPresetController(FolderPresetService folderPresetService) {
        this.folderPresetService = folderPresetService;
    }

    @GetMapping
    public List<FolderPresetResponse> list(@AuthenticationPrincipal User user) {
        return folderPresetService.findByUser(user.getId());
    }

    @PostMapping
    public FolderPresetResponse create(@AuthenticationPrincipal User user,
                                       @Valid @RequestBody FolderPresetRequest request) {
        return folderPresetService.create(user.getId(), request);
    }

    @PutMapping("/{id}")
    public FolderPresetResponse update(@AuthenticationPrincipal User user,
                                       @PathVariable Long id,
                                       @Valid @RequestBody FolderPresetRequest request) {
        return folderPresetService.update(user.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        folderPresetService.delete(user.getId(), id);
        return new MessageResponse("delete success");
    }

    @GetMapping("/{id}/images/index")
    public List<FolderImageIndexResponse> imageIndex(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return folderPresetService.findImageIndex(user.getId(), id);
    }

    @PostMapping(value = "/{id}/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FolderImageUploadResponse uploadImage(@AuthenticationPrincipal User user,
                                                 @PathVariable Long id,
                                                 @RequestParam("file") MultipartFile file,
                                                 @RequestParam(required = false) String title,
                                                 @RequestParam(required = false) String description,
                                                 @RequestParam(required = false) String category,
                                                 @RequestParam(required = false) String tags,
                                                 @RequestParam(required = false) String sourceName,
                                                 @RequestParam(required = false) String originalPath,
                                                 @RequestParam(required = false) String relativePath,
                                                 @RequestParam(required = false) Long lastModified) {
        return folderPresetService.uploadFolderImage(
                user.getId(),
                id,
                file,
                title,
                description,
                category,
                tags,
                originalPath,
                relativePath,
                lastModified
        );
    }

    @PostMapping("/{id}/cleanup-missing")
    public FolderSyncResultResponse cleanupMissing(@AuthenticationPrincipal User user,
                                                   @PathVariable Long id,
                                                   @RequestBody FolderCleanupRequest request) {
        return folderPresetService.cleanupMissing(user.getId(), id, request);
    }
}
