package com.example.imagemanager.controller;

import com.example.imagemanager.dto.ImageResponse;
import com.example.imagemanager.dto.MessageResponse;
import com.example.imagemanager.dto.StorageContent;
import com.example.imagemanager.dto.TagRequest;
import com.example.imagemanager.entity.ImageItem;
import com.example.imagemanager.entity.User;
import com.example.imagemanager.service.ImageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    public List<ImageResponse> listImages(@AuthenticationPrincipal User user,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String source,
                                          @RequestParam(required = false) String tag,
                                          @RequestParam(required = false) String timeRange,
                                          @RequestParam(required = false) String sort) {
        return imageService.findImages(user.getId(), keyword, source, tag, timeRange, sort);
    }

    @GetMapping("/sources")
    public List<String> sources(@AuthenticationPrincipal User user) {
        return imageService.findSources(user.getId());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageResponse upload(@AuthenticationPrincipal User user,
                                @RequestParam("file") MultipartFile file,
                                @RequestParam(required = false) String title,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String tags,
                                @RequestParam(required = false) String sourceName,
                                @RequestParam(required = false) String sourceType,
                                @RequestParam(required = false) String originalPath,
                                @RequestParam(required = false) String relativePath,
                                @RequestParam(required = false) Long lastModified) {
        return imageService.uploadImage(
                file,
                user.getId(),
                title,
                description,
                category,
                tags,
                sourceName,
                sourceType,
                originalPath,
                relativePath,
                lastModified
        );
    }

    @GetMapping("/{id}")
    public ImageResponse detail(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return imageService.findImage(id, user.getId());
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        imageService.deleteImage(id, user.getId());
        return new MessageResponse("delete success");
    }

    @PostMapping("/{id}/tags/toggle")
    public ImageResponse toggleTag(@AuthenticationPrincipal User user,
                                   @PathVariable Long id,
                                   @Valid @RequestBody TagRequest request) {
        return imageService.toggleTag(id, user.getId(), request.getTag());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Void> download(@AuthenticationPrincipal User user, @PathVariable Long id) {
        ImageItem image = imageService.findImageEntity(id, user.getId());
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, image.getPublicUrl())
                .location(URI.create(image.getPublicUrl()))
                .build();
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<StreamingResponseBody> content(@AuthenticationPrincipal User user, @PathVariable Long id) {
        StorageContent content = imageService.loadImageContent(id, user.getId());
        StreamingResponseBody body = outputStream -> {
            try (var inputStream = content.getInputStream()) {
                inputStream.transferTo(outputStream);
            }
        };

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.getContentType()));
        if (content.getContentLength() != null && content.getContentLength() >= 0) {
            builder.contentLength(content.getContentLength());
        }
        return builder.body(body);
    }
}
