package com.xanqan.project.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String upload(String bucketName, String objectName , MultipartFile multipartFile);
}
