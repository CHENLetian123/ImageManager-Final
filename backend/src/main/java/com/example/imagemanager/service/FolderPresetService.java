package com.example.imagemanager.service;

import com.example.imagemanager.dto.FolderCleanupRequest;
import com.example.imagemanager.dto.FolderImageIndexResponse;
import com.example.imagemanager.dto.FolderImageUploadResponse;
import com.example.imagemanager.dto.FolderPresetRequest;
import com.example.imagemanager.dto.FolderPresetResponse;
import com.example.imagemanager.dto.FolderSyncResultResponse;
import com.example.imagemanager.dto.ImageResponse;
import com.example.imagemanager.dto.StorageResult;
import com.example.imagemanager.entity.FolderPreset;
import com.example.imagemanager.entity.ImageItem;
import com.example.imagemanager.repository.FolderPresetRepository;
import com.example.imagemanager.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class FolderPresetService {

    private final FolderPresetRepository folderPresetRepository;
    private final ImageRepository imageRepository;
    private final List<StorageService> storageServices;
    private final String storageType;

    public FolderPresetService(FolderPresetRepository folderPresetRepository,
                               ImageRepository imageRepository,
                               List<StorageService> storageServices,
                               @Value("${app.storage-type:local}") String storageType) {
        this.folderPresetRepository = folderPresetRepository;
        this.imageRepository = imageRepository;
        this.storageServices = storageServices;
        this.storageType = storageType;
    }

    public List<FolderPresetResponse> findByUser(Long userId) {
        return folderPresetRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(FolderPresetResponse::from)
                .toList();
    }

    public FolderPresetResponse create(Long userId, FolderPresetRequest request) {
        FolderPreset preset = new FolderPreset();
        preset.setUserId(userId);
        preset.setName(request.getName().trim());
        preset.setSourcePath(normalizePath(request.getSourcePath()));
        return FolderPresetResponse.from(folderPresetRepository.save(preset));
    }

    public FolderPresetResponse update(Long userId, Long id, FolderPresetRequest request) {
        FolderPreset preset = findEntity(id, userId);
        preset.setName(request.getName().trim());
        preset.setSourcePath(normalizePath(request.getSourcePath()));
        return FolderPresetResponse.from(folderPresetRepository.save(preset));
    }

    public void delete(Long userId, Long id) {
        FolderPreset preset = findEntity(id, userId);
        folderPresetRepository.delete(preset);
    }

    public List<FolderImageIndexResponse> findImageIndex(Long userId, Long presetId) {
        findEntity(presetId, userId);
        return imageRepository.findByUserIdAndFolderPresetIdOrderByRelativePathAsc(userId, presetId)
                .stream()
                .map(FolderImageIndexResponse::from)
                .toList();
    }

    @Transactional
    public FolderImageUploadResponse uploadFolderImage(Long userId,
                                                       Long presetId,
                                                       MultipartFile file,
                                                       String title,
                                                       String description,
                                                       String category,
                                                       String tags,
                                                       String originalPath,
                                                       String relativePath,
                                                       Long lastModified) {
        FolderPreset preset = findEntity(presetId, userId);
        validateImageFile(file);

        String normalizedRelativePath = normalizePath(hasText(relativePath) ? relativePath : file.getOriginalFilename());
        String normalizedOriginalPath = normalizePath(hasText(originalPath)
                ? originalPath
                : buildOriginalPath(preset, normalizedRelativePath));
        Long currentLastModified = lastModified != null ? lastModified : System.currentTimeMillis();

        ImageItem existing = imageRepository.findByUserIdAndFolderPresetIdAndRelativePath(userId, presetId, normalizedRelativePath)
                .orElse(null);
        if (existing != null
                && Objects.equals(existing.getFileSize(), file.getSize())
                && Objects.equals(existing.getLastModified(), currentLastModified)) {
            markSynced(preset);
            return new FolderImageUploadResponse("skipped", ImageResponse.from(existing));
        }

        StorageResult storageResult = chooseStorage().upload(file, userId);
        String oldObjectKey = existing == null ? null : existing.getR2ObjectKey();
        ImageItem image = existing == null ? new ImageItem() : existing;
        image.setUserId(userId);
        image.setFolderPresetId(presetId);
        image.setTitle(hasText(title) ? title.trim() : file.getOriginalFilename());
        image.setDescription(trimToNull(description));
        image.setCategory(hasText(category) ? category.trim() : "Uncategorized");
        image.setTags(normalizeTags(tags));
        image.setSourceType("FOLDER_SYNC");
        image.setSourceName(resolveSourceName(preset));
        image.setOriginalFileName(file.getOriginalFilename());
        image.setOriginalPath(normalizedOriginalPath);
        image.setRelativePath(normalizedRelativePath);
        image.setR2ObjectKey(storageResult.getObjectKey());
        image.setPublicUrl(storageResult.getPublicUrl());
        image.setContentType(storageResult.getContentType());
        image.setFileSize(storageResult.getFileSize());
        image.setLastModified(currentLastModified);
        ImageItem saved = imageRepository.save(image);
        markSynced(preset);

        if (oldObjectKey != null && !oldObjectKey.equals(saved.getR2ObjectKey())) {
            chooseStorage().delete(oldObjectKey);
        }

        return new FolderImageUploadResponse(existing == null ? "uploaded" : "updated", ImageResponse.from(saved));
    }

    @Transactional
    public FolderSyncResultResponse cleanupMissing(Long userId, Long presetId, FolderCleanupRequest request) {
        FolderPreset preset = findEntity(presetId, userId);
        if (request == null || !request.isDeleteMissing()) {
            markSynced(preset);
            return new FolderSyncResultResponse(0, 0, 0, 0);
        }

        Set<String> currentPaths = new HashSet<>();
        if (request.getCurrentRelativePaths() != null) {
            request.getCurrentRelativePaths().stream()
                    .map(this::normalizePath)
                    .filter(this::hasText)
                    .forEach(currentPaths::add);
        }

        int deleted = 0;
        List<ImageItem> existing = imageRepository.findByUserIdAndFolderPresetIdOrderByRelativePathAsc(userId, presetId);
        for (ImageItem image : existing) {
            String relativePath = normalizePath(image.getRelativePath());
            if (!currentPaths.contains(relativePath)) {
                try {
                    chooseStorage().delete(image.getR2ObjectKey());
                } catch (RuntimeException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                            "Storage delete failed for " + relativePath + ".", e);
                }
                imageRepository.delete(image);
                deleted++;
            }
        }
        markSynced(preset);
        return new FolderSyncResultResponse(0, 0, 0, deleted);
    }

    private FolderPreset findEntity(Long id, Long userId) {
        return folderPresetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder preset not found."));
    }

    private void markSynced(FolderPreset preset) {
        preset.setLastSyncAt(LocalDateTime.now());
        folderPresetRepository.save(preset);
    }

    private StorageService chooseStorage() {
        String expected = hasText(storageType) ? storageType.trim().toLowerCase(Locale.ROOT) : "local";
        StorageService selected = storageServices.stream()
                .filter(service -> service.getName().equalsIgnoreCase(expected))
                .findFirst()
                .orElse(null);
        if (selected != null && selected.isAvailable()) {
            return selected;
        }
        return storageServices.stream()
                .filter(service -> service.getName().equalsIgnoreCase("local"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available storage service."));
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required.");
        }
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        boolean contentTypeOk = contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/");
        boolean extensionOk = hasAllowedExtension(fileName);
        if (!contentTypeOk && !extensionOk) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed.");
        }
    }

    private boolean hasAllowedExtension(String fileName) {
        if (!hasText(fileName)) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp")
                || lower.endsWith(".bmp");
    }

    private String resolveSourceName(FolderPreset preset) {
        return hasText(preset.getSourcePath()) ? preset.getSourcePath() : preset.getName();
    }

    private String buildOriginalPath(FolderPreset preset, String relativePath) {
        if (!hasText(preset.getSourcePath())) {
            return relativePath;
        }
        return trimTrailingSlash(preset.getSourcePath()) + "/" + relativePath;
    }

    private String normalizeTags(String tags) {
        if (!hasText(tags)) {
            return "";
        }
        return String.join(",", Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(this::hasText)
                .distinct()
                .toList());
    }

    private String normalizePath(String path) {
        if (!hasText(path)) {
            return "";
        }
        return path.trim().replace("\\", "/");
    }

    private String trimTrailingSlash(String value) {
        String result = normalizePath(value);
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
