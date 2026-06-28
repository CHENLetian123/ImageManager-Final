package com.example.imagemanager.service;

import com.example.imagemanager.dto.ImageResponse;
import com.example.imagemanager.dto.StorageContent;
import com.example.imagemanager.dto.StorageResult;
import com.example.imagemanager.entity.ImageItem;
import com.example.imagemanager.repository.ImageRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final List<StorageService> storageServices;
    private final String storageType;

    public ImageService(ImageRepository imageRepository,
                        List<StorageService> storageServices,
                        @Value("${app.storage-type:local}") String storageType) {
        this.imageRepository = imageRepository;
        this.storageServices = storageServices;
        this.storageType = storageType;
    }

    public List<ImageResponse> findImages(Long userId, String keyword, String source, String tag, String timeRange, String sort) {
        Specification<ImageItem> specification = buildSpecification(userId, keyword, source, tag, timeRange);
        return imageRepository.findAll(specification, buildSort(sort))
                .stream()
                .map(ImageResponse::from)
                .toList();
    }

    public ImageResponse findImage(Long id, Long userId) {
        return ImageResponse.from(findImageEntity(id, userId));
    }

    public ImageItem findImageEntity(Long id, Long userId) {
        return imageRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found."));
    }

    public ImageResponse uploadImage(MultipartFile file,
                                     Long userId,
                                     String title,
                                     String description,
                                     String category,
                                     String tags,
                                     String sourceName,
                                     String sourceType,
                                     String originalPath,
                                     String relativePath,
                                     Long lastModified) {
        validateImageFile(file);

        StorageResult storageResult = chooseStorage().upload(file, userId);
        String originalFileName = file.getOriginalFilename();
        String normalizedRelativePath = normalizePath(relativePath);
        String normalizedOriginalPath = normalizePath(originalPath);

        ImageItem image = new ImageItem();
        image.setUserId(userId);
        image.setTitle(hasText(title) ? title.trim() : originalFileName);
        image.setDescription(trimToNull(description));
        image.setCategory(hasText(category) ? category.trim() : "Uncategorized");
        image.setTags(normalizeTags(tags));
        image.setSourceType(resolveSourceType(sourceType, normalizedRelativePath));
        image.setSourceName(hasText(sourceName) ? sourceName.trim() : "Upload");
        image.setOriginalFileName(originalFileName);
        image.setOriginalPath(normalizedOriginalPath);
        image.setRelativePath(normalizedRelativePath);
        image.setR2ObjectKey(storageResult.getObjectKey());
        image.setPublicUrl(storageResult.getPublicUrl());
        image.setContentType(storageResult.getContentType());
        image.setFileSize(storageResult.getFileSize());
        image.setLastModified(lastModified != null ? lastModified : System.currentTimeMillis());
        return ImageResponse.from(imageRepository.save(image));
    }

    public void deleteImage(Long id, Long userId) {
        ImageItem image = findImageEntity(id, userId);
        chooseStorageForObject(image.getR2ObjectKey()).delete(image.getR2ObjectKey());
        imageRepository.delete(image);
    }

    public StorageContent loadImageContent(Long id, Long userId) {
        ImageItem image = findImageEntity(id, userId);
        return chooseStorageForObject(image.getR2ObjectKey()).load(image.getR2ObjectKey(), image.getContentType());
    }

    public ImageResponse toggleTag(Long id, Long userId, String tag) {
        if (!hasText(tag)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag is required.");
        }

        ImageItem image = findImageEntity(id, userId);
        Set<String> tagSet = new LinkedHashSet<>();
        if (hasText(image.getTags())) {
            Arrays.stream(image.getTags().split(","))
                    .map(String::trim)
                    .filter(this::hasText)
                    .forEach(tagSet::add);
        }

        String normalizedTag = tag.trim();
        boolean exists = tagSet.stream().anyMatch(value -> value.equalsIgnoreCase(normalizedTag));
        if (exists) {
            tagSet.removeIf(value -> value.equalsIgnoreCase(normalizedTag));
        } else {
            tagSet.add(normalizedTag);
        }
        image.setTags(String.join(",", tagSet));
        return ImageResponse.from(imageRepository.save(image));
    }

    public List<String> findSources(Long userId) {
        return imageRepository.findDistinctSourceNamesByUserId(userId);
    }

    private Specification<ImageItem> buildSpecification(Long userId, String keyword, String source, String tag, String timeRange) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("userId"), userId));

            if (hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("title")), like),
                        builder.like(builder.lower(root.get("description")), like),
                        builder.like(builder.lower(root.get("category")), like),
                        builder.like(builder.lower(root.get("tags")), like),
                        builder.like(builder.lower(root.get("originalFileName")), like),
                        builder.like(builder.lower(root.get("sourceName")), like),
                        builder.like(builder.lower(root.get("originalPath")), like)
                ));
            }

            if (hasText(source)) {
                predicates.add(builder.equal(root.get("sourceName"), source.trim()));
            }

            if (hasText(tag)) {
                String like = "%" + tag.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.like(builder.lower(root.get("tags")), like));
            }

            TimeRange range = resolveTimeRange(timeRange);
            if (range.from != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("lastModified"), range.from));
            }
            if (range.to != null) {
                predicates.add(builder.lessThan(root.get("lastModified"), range.to));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(String sort) {
        if ("title".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "title");
        }
        if ("source".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "sourceName");
        }
        if ("size".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "fileSize");
        }
        if ("created".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return Sort.by(Sort.Direction.DESC, "lastModified");
    }

    private TimeRange resolveTimeRange(String timeRange) {
        if (!hasText(timeRange)) {
            return new TimeRange(null, null);
        }

        LocalDate today = LocalDate.now();
        LocalDate startOfThisWeek = today.with(DayOfWeek.MONDAY);
        LocalDate startOfLastWeek = startOfThisWeek.minusWeeks(1);
        long thisWeek = toMillis(startOfThisWeek);
        long lastWeek = toMillis(startOfLastWeek);

        if ("thisWeek".equalsIgnoreCase(timeRange)) {
            return new TimeRange(thisWeek, null);
        }
        if ("lastWeek".equalsIgnoreCase(timeRange)) {
            return new TimeRange(lastWeek, thisWeek);
        }
        if ("older".equalsIgnoreCase(timeRange)) {
            return new TimeRange(null, lastWeek);
        }
        return new TimeRange(null, null);
    }

    private long toMillis(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
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

    private StorageService chooseStorageForObject(String objectKey) {
        if (hasText(objectKey) && objectKey.startsWith("users/")) {
            return chooseStorage();
        }
        return chooseStorage();
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

    private String resolveSourceType(String sourceType, String relativePath) {
        if (hasText(sourceType)) {
            return sourceType.trim();
        }
        return hasText(relativePath) && relativePath.contains("/") ? "BATCH_UPLOAD" : "UPLOAD";
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

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record TimeRange(Long from, Long to) {
    }
}
