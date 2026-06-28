package com.example.imagemanager.service;

import com.example.imagemanager.dto.StorageResult;
import com.example.imagemanager.dto.StorageContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
public class R2StorageService implements StorageService {

    @Value("${app.r2.endpoint:}")
    private String endpoint;

    @Value("${app.r2.access-key-id:}")
    private String accessKeyId;

    @Value("${app.r2.secret-access-key:}")
    private String secretAccessKey;

    @Value("${app.r2.bucket:}")
    private String bucket;

    @Value("${app.r2.public-base-url:}")
    private String publicBaseUrl;

    private S3Client s3Client;

    @Override
    public String getName() {
        return "r2";
    }

    @Override
    public boolean isAvailable() {
        return hasText(endpoint)
                && hasText(accessKeyId)
                && hasText(secretAccessKey)
                && hasText(bucket)
                && hasText(publicBaseUrl);
    }

    @Override
    public StorageResult upload(MultipartFile file, Long userId) {
        if (!isAvailable()) {
            throw new IllegalStateException("R2 storage is not configured.");
        }
        try {
            String extension = getExtension(file.getOriginalFilename());
            String objectKey = "users/" + userId + "/" + UUID.randomUUID() + extension;
            String contentType = normalizeContentType(file);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            getClient().putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return new StorageResult(objectKey, getPublicUrl(objectKey), contentType, file.getSize());
        } catch (IOException e) {
            throw new IllegalStateException("R2 upload failed.", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        if (!isAvailable() || objectKey == null || objectKey.isBlank()) {
            return;
        }
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
        getClient().deleteObject(request);
    }

    @Override
    public StorageContent load(String objectKey, String fallbackContentType) {
        if (!isAvailable()) {
            throw new IllegalStateException("R2 storage is not configured.");
        }
        if (!hasText(objectKey)) {
            throw new IllegalStateException("R2 object key is empty.");
        }

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            ResponseInputStream<GetObjectResponse> stream = getClient().getObject(request);
            GetObjectResponse response = stream.response();
            String contentType = hasText(response.contentType()) ? response.contentType() : normalizeContentType(fallbackContentType);
            return new StorageContent(stream, contentType, response.contentLength());
        } catch (SdkException e) {
            throw new IllegalStateException("R2 image read failed.", e);
        }
    }

    @Override
    public String getPublicUrl(String objectKey) {
        return trimEnd(publicBaseUrl, "/") + "/" + objectKey;
    }

    private S3Client getClient() {
        if (s3Client == null) {
            s3Client = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                    .region(Region.of("auto"))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build())
                    .build();
        }
        return s3Client;
    }

    private String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        return index >= 0 ? fileName.substring(index).toLowerCase() : "";
    }

    private String normalizeContentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }

    private String normalizeContentType(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimEnd(String value, String suffix) {
        while (value.endsWith(suffix)) {
            value = value.substring(0, value.length() - suffix.length());
        }
        return value;
    }
}
