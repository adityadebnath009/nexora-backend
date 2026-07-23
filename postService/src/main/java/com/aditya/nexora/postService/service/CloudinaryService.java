package com.aditya.nexora.postService.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public List<String> uploadImages(List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return imageUrls;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            try {
                log.info("Uploading file to Cloudinary: {}", file.getOriginalFilename());
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                String secureUrl = (String) uploadResult.get("secure_url");
                imageUrls.add(secureUrl);
                log.info("Successfully uploaded file to Cloudinary. URL: {}", secureUrl);
            } catch (IOException e) {
                log.error("Failed to upload file to Cloudinary", e);
                throw new RuntimeException("Failed to upload post image", e);
            }
        }
        return imageUrls;
    }
}
