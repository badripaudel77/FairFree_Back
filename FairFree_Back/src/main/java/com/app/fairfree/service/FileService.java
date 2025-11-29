package com.app.fairfree.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    // upload to S3 and return the URL
    @Transactional
    public String uploadImagesToCloud(MultipartFile file) {
        // TODO:
        System.out.println("Images Uploaded to S3 " + file.getOriginalFilename());
        return "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/250px-Image_created_with_a_mobile_phone.png";
    }
}
