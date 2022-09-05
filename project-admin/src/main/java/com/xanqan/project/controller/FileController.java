package com.xanqan.project.controller;

import com.xanqan.project.common.BaseResponse;
import com.xanqan.project.common.ResultUtils;
import com.xanqan.project.model.dto.File;
import com.xanqan.project.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @Operation(summary = "获取路径下全部文件")
    @PostMapping("/getFileList")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<List<File>> getFileList(@RequestParam("path") String path,
                                                HttpServletRequest request) {
        List<File> result = fileService.getFileList(path, request);
        return ResultUtils.success(result);
    }

    @Operation(summary = "创建文件夹")
    @PostMapping("/createFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> createFolder(@RequestParam("path") String path,
                                              @RequestParam("folderName") String folderName,
                                              HttpServletRequest request) {
        boolean result = fileService.createFolder(path, folderName, request);
        return ResultUtils.success(result);
    }

    @Operation(summary = "删除文件夹")
    @PostMapping("/deleteFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> deleteFolder(@RequestParam("path") String path,
                                              @RequestParam("folderName") String folderName,
                                              HttpServletRequest request) {
        boolean result = fileService.deleteFolder(path, folderName, request);
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<String> upload(@RequestParam("path") String path,
                                       @RequestParam("file") MultipartFile multipartFile,
                                       HttpServletRequest request) {
        String result = fileService.upload(path, multipartFile, request);
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> delete(@RequestParam("path") String path,
                                        @RequestParam("fileName") String fileName,
                                        HttpServletRequest request) {
        boolean result = fileService.remove(path, fileName, request);
        return ResultUtils.success(result);
    }
}
