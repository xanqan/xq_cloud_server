package com.xanqan.project.service.impl;

import com.xanqan.project.util.FileUtil;

import com.xanqan.project.model.dto.File;
import com.xanqan.project.service.FileService;
import com.xanqan.project.util.MinioUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Service
public class FileServiceImpl implements FileService {

    @Resource
    private MinioUtil minioUtil;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private FileUtil fileUtil;

    @Override
    public String upload(String bucketName, String objectName, MultipartFile multipartFile) {
        minioUtil.upload(bucketName, objectName, multipartFile);
        File file = fileUtil.read(objectName, multipartFile);
        File result = mongoTemplate.insert(file, bucketName);
        return result.getName();
    }
}
