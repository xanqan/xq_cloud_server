package com.xanqan.project.controller;

import cn.hutool.json.JSONUtil;
import com.xanqan.project.common.BaseResponse;
import com.xanqan.project.common.ResultUtils;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.model.dto.File;
import com.xanqan.project.model.dto.Share;
import com.xanqan.project.model.vo.*;
import com.xanqan.project.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Operation(summary = "根据类型获取文件，分页")
    @GetMapping("/getFileListByType")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Home> getFileListByType(@RequestParam("type") String type,
                                                @RequestParam("page") Integer page,
                                                @RequestParam("rows") Integer rows,
                                                @RequestParam("user") String user) {
        List<File> result = fileService.getFileListByType(type, page, rows, JSONUtil.toBean(user, User.class));
        UserInfo userInfo = new UserInfo(JSONUtil.toBean(user, User.class));
        Home home = new Home(userInfo, result, null);
        return ResultUtils.success(home);
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
    public BaseResponse<File> createFolder(@RequestBody FileInfo fileInfo,
                                           @RequestParam("user") String user) {
        File result = fileService.createFolder(fileInfo.getPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件夹批量创建")
    @PostMapping("/createFolderBatch")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<File> createFolderBatch(@RequestBody List<String> pathSet,
                                                @RequestParam("user") String user) {
        List<File> files = new ArrayList<>();
        for (String value : pathSet) {
            int index = value.lastIndexOf("/");
            if (index == 0) {
                files.add(fileService.createFolder("/", value.substring(index + 1), JSONUtil.toBean(user, User.class)));
            } else {
                files.add(fileService.createFolder(value.substring(0, index), value.substring(index + 1), JSONUtil.toBean(user, User.class)));
            }
        }
        return ResultUtils.success(files.size() > 0 ? files.get(0) : null);
    }

    @Operation(summary = "文件夹删除")
    @DeleteMapping("/deleteFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> deleteFolder(@RequestBody FileInfo fileInfo,
                                              @RequestParam("user") String user) {
        boolean result = fileService.deleteFolder(fileInfo.getPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件夹重命名")
    @PutMapping("/reNameFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> reNameFolder(@RequestBody FileInfo fileInfo,
                                              @RequestParam("user") String user) {
        boolean result = fileService.reNameFolder(fileInfo.getPath(), fileInfo.getName(), fileInfo.getNewName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件夹移动")
    @PutMapping("/moveFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> moveFolder(@RequestBody FileInfo fileInfo,
                                            @RequestParam("user") String user) {
        boolean result = fileService.moveFolder(fileInfo.getPath(), fileInfo.getNewPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件夹复制")
    @PutMapping("/copyFolder")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> copyFolder(@RequestBody FileInfo fileInfo,
                                            @RequestParam("user") String user) {
        boolean result = fileService.copyFolder(fileInfo.getPath(), fileInfo.getNewPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件上传")
    @PutMapping("/upload")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<File> upload(@RequestParam("path") String path,
                                     @RequestParam("file") MultipartFile multipartFile,
                                     @RequestParam("user") String user) {
        File result = fileService.upload(path, multipartFile, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件删除")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> delete(@RequestBody FileInfo fileInfo,
                                        @RequestParam("user") String user) {
        boolean result = fileService.remove(fileInfo.getPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件重命名")
    @PutMapping("/reName")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> reName(@RequestBody FileInfo fileInfo,
                                        @RequestParam("user") String user) {
        boolean result = fileService.reName(fileInfo.getPath(), fileInfo.getName(), fileInfo.getNewName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件移动")
    @PutMapping("/move")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> move(@RequestBody FileInfo fileInfo,
                                      @RequestParam("user") String user) {
        boolean result = fileService.move(fileInfo.getPath(), fileInfo.getNewPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "文件复制")
    @PutMapping("/copy")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> copy(@RequestBody FileInfo fileInfo,
                                      @RequestParam("user") String user) {
        boolean result = fileService.copy(fileInfo.getPath(), fileInfo.getNewPath(), fileInfo.getName(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "大文件上传初始化")
    @PostMapping("/initBigFileUpload")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<List<FileChunk>> initBigFileUpload(@RequestBody FileInfo fileInfo,
                                                           @RequestParam("user") String user) {
        List<FileChunk> result = fileService.initBigFileUpload(fileInfo.getPath(), fileInfo.getName(), fileInfo.getFileChunks(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "大文件分片上传")
    @PutMapping("/bigFileUpload")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<File> bigFileUpload(@RequestParam("path") String path,
                                            @RequestParam("chunkId") String chunkId,
                                            @RequestParam("file") MultipartFile multipartFile,
                                            @RequestParam("user") String user) {
        File result = fileService.bigFileUpload(path, chunkId, multipartFile, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "获取文件分享链接")
    @PostMapping("/createShareUrl")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Share> createShareUrl(@RequestBody ShareInfo share,
                                              @RequestParam("user") String user) {
        Share result = fileService.createShareUrl(share.getPath(), share.getName(), share.getPassword(), share.getExpire(), JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "获取用户全部分享链接")
    @GetMapping("/getShareUrlAll")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Home> getShareUrlAll(@RequestParam("user") String user) {
        List<Share> result = fileService.getShareUrlAll(JSONUtil.toBean(user, User.class));
        UserInfo userInfo = new UserInfo(JSONUtil.toBean(user, User.class));
        Home home = new Home(userInfo, null, result);
        return ResultUtils.success(home);
    }

    @Operation(summary = "删除分享链接")
    @DeleteMapping("/removeShareUrl")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> removeShareUrl(@RequestParam("id") String id,
                                                @RequestParam("user") String user) {
        boolean result = fileService.removeShareUrl(id, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "下载分享")
    @GetMapping("/getShareUrl")
    public BaseResponse<Share> getShareUrl(@RequestParam("shareId") String shareId) {
        Share result = fileService.getShareUrl(shareId);
        return ResultUtils.success(result);
    }
}
