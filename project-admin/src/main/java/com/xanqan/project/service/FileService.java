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
     * 获取路径下的所有文件（包含文件夹）
     *
     * @param path 路径
     * @param request 用于获取 token
     * @return 文件列表
     */
    List<File> getFileList(String path, HttpServletRequest request);

    /**
     * 获取文件夹的总大小
     *
     * @param path 路径
     * @param folderName 文件夹名称
     * @param request 用于获取 token
     * @return 文件夹总大小（byte）
     */
    long getFolderSize(String path, String folderName, HttpServletRequest request);

    /**
     * 获取文件夹的总大小
     *
     * @param path 路径
     * @param folderName 文件夹名称
     * @param request 用于获取 token
     * @return boolean
     */
    boolean createFolder(String path, String folderName, HttpServletRequest request);

    /**
     * 获取文件夹的总大小
     *
     * @param path 路径
     * @param folderName 文件夹名称
     * @param request 用于获取 token
     * @return boolean
     */
    boolean deleteFolder(String path, String folderName, HttpServletRequest request);

    /**
     * 文件上传
     *
     * @param path 文件的路径
     * @param multipartFile 文件封装
     * @param request 用于获取 token
     * @return 文件的可访问路径
     */
    String upload(String path, MultipartFile multipartFile, HttpServletRequest request);

    /**
     * 文件上传
     *
     * @param path 文件的路径
     * @param fileName 文件名
     * @param request 用于获取 token
     * @return 文件的可访问路径
     */
    boolean remove(String path, String fileName, HttpServletRequest request);










}
