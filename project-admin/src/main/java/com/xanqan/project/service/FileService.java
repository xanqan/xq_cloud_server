package com.xanqan.project.service;

import com.xanqan.project.model.dto.File;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 文件服务接口
 *
 * @author xanqan
 */
public interface FileService {

    /**
     * 文件上传
     *
     * @param path 文件的路径
     * @param multipartFile 文件封装
     * @return 文件的可访问路径
     */
    String upload(String path, MultipartFile multipartFile, HttpServletRequest request);

    boolean remove(String path, String fileName, HttpServletRequest request);

    boolean createFolder(String path, String folderName, HttpServletRequest request);

    boolean deleteFolder(String path, String folderName, HttpServletRequest request);

    List<File> getFileList(String path, HttpServletRequest request);

}
