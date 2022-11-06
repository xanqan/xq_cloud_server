package com.xanqan.project.controller;

import cn.hutool.json.JSONUtil;
import com.xanqan.project.common.BaseResponse;
import com.xanqan.project.common.ResultUtils;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.model.dto.File;
import com.xanqan.project.model.vo.FileInfo;
import com.xanqan.project.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
    @GetMapping("/getFileList")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<List<File>> getFileList(@RequestParam("path") String path,
                                                @RequestParam("user") String user) {
        List<File> result = fileService.getFileList(path, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "获取文件夹大小")
    @PostMapping("/getFolderSize")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Long> getFolderSize(@RequestParam("path") String path,
                                            @RequestParam("folderName") String folderName,
                                            @RequestParam("user") String user) {
        long result = fileService.getFolderSize(path, folderName, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件夹创建")
    @PostMapping("/createFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<File> createFolder(@RequestParam("path") String path,
                                           @RequestParam("folderName") String folderName,
                                           @RequestParam("user") String user) {
        File result = fileService.createFolder(path, folderName, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件夹删除")
    @PostMapping("/deleteFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> deleteFolder(@RequestBody FileInfo fileInfo,
                                              @RequestParam("user") String user) {
        boolean result = fileService.deleteFolder(fileInfo.getPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件夹重命名")
    @PostMapping("/reNameFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> reNameFolder(@RequestParam("path") String path,
                                              @RequestParam("oldName") String oldName,
                                              @RequestParam("newName") String newName,
                                              @RequestParam("user") String user) {
        boolean result = fileService.reNameFolder(path, oldName, newName, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件夹移动")
    @PostMapping("/moveFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> moveFolder(@RequestParam("oldPath") String oldPath,
                                            @RequestParam("newPath") String newPath,
                                            @RequestParam("folderName") String folderName,
                                            @RequestParam("user") String user) {
        boolean result = fileService.moveFolder(oldPath, newPath, folderName, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件夹复制")
    @PostMapping("/copyFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> copyFolder(@RequestParam("oldPath") String oldPath,
                                            @RequestParam("newPath") String newPath,
                                            @RequestParam("folderName") String folderName,
                                            @RequestParam("user") String user) {
        boolean result = fileService.copyFolder(oldPath, newPath, folderName, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<File> upload(@RequestParam("path") String path,
                                     @RequestParam("file") MultipartFile multipartFile,
                                     @RequestParam("user") String user) {
        File result = fileService.upload(path, multipartFile, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> delete(@RequestBody FileInfo fileInfo,
                                        @RequestParam("user") String user) {
        boolean result = fileService.remove(fileInfo.getPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件重命名")
    @PostMapping("/reName")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> reName(@RequestParam("path") String path,
                                        @RequestParam("oldName") String oldName,
                                        @RequestParam("newName") String newName,
                                        @RequestParam("user") String user) {
        boolean result = fileService.reName(path, oldName, newName, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件移动")
    @PostMapping("/move")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> move(@RequestBody FileInfo fileInfo,
                                      @RequestParam("user") String user) {
        boolean result = fileService.move(fileInfo.getPath(), fileInfo.getNewPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件复制")
    @PostMapping("/copy")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> copy(@RequestBody FileInfo fileInfo,
                                      @RequestParam("user") String user) {
        boolean result = fileService.copy(fileInfo.getPath(), fileInfo.getNewPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }
}
