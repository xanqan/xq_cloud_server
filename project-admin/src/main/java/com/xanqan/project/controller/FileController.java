package com.xanqan.project.controller;

import com.xanqan.project.common.BaseResponse;
import com.xanqan.project.common.ResultUtils;
import com.xanqan.project.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 文件请求处理
 *
 * @author xanqan
 */
@RestController
@RequestMapping("/file")
@Api(tags = "文件请求处理")
public class FileController {

    @Resource
    private FileService fileService;

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<String> upload(@RequestParam("path") String path,
                                       @RequestParam("file") MultipartFile multipartFile,
                                       HttpServletRequest request) {
        String result = fileService.upload(path, multipartFile, request);
        return ResultUtils.success(result);
    }
}
