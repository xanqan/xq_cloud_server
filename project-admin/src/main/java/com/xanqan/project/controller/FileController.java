package com.xanqan.project.controller;

import com.xanqan.project.common.BaseResponse;
import com.xanqan.project.common.ResultUtils;
import com.xanqan.project.util.MinioUtil;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/file")
@Api(tags = "文件请求处理")
public class FileController {

    @Resource
    private MinioUtil minioUtil;

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public BaseResponse<String> upload(@RequestParam("bucketName") String bucketName,
                                       @RequestParam("objectName") String objectName,
                                       @RequestParam("file") MultipartFile file) {
        String upload = minioUtil.upload(bucketName, objectName, file);
        return ResultUtils.success(upload);
    }
}
