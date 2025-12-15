package com.app.fairfree.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.*;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final S3Client client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${aws.s3.base-folder:fairfree-item-images/}")
    private String baseFolder;

    public FileService(S3Client client) {
        this.client = client;
    }

    // Upload a single image to S3 under item-specific folder
    @Transactional
    public Map<String, String> uploadImage(MultipartFile file, Long itemId) {
        if (!"prod".equalsIgnoreCase(activeProfile)) {
            System.out.println("not a prod env");
            Map<String, String> map = new HashMap<>();
            map.put("key", "MY_KEY");
            map.put("url", "https://fairfree-item-images.s3.us-east-1.amazonaws.com/fairfree-item-images/item/9/27fd276e-ea51-48ed-82a5-a28cc17b011f_for_Loop.png");
            return map;
        }
        try {
            System.out.println("this is a prod env");
            String safeFileName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
            String key = baseFolder + "item/" + itemId + "/" + UUID.randomUUID() + "_" + safeFileName;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            String url = buildS3Url(key);
            logger.info("Uploaded file '{}' to S3 bucket '{}' with key '{}' and url '{}'",
                    file.getOriginalFilename(), bucket, key, url);

            Map<String, String> result = new HashMap<>();
            result.put("key", key);
            result.put("url", url);
            return result;
        } catch (IOException e) {
            logger.error("Failed to upload file {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename());
        }
    }

    // Delete single image from S3 using its key
    @Transactional
    public void deleteImage(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        client.deleteObject(request);
        logger.info("Deleted S3 object with key '{}'", key);
    }

    // Delete multiple images from S3
    @Transactional
    public void deleteImages(List<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                deleteImage(key);
            }
        }
    }

    // Build public URL from S3 key
    private String buildS3Url(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucket, key);
    }
}
