package com.xanqan.project.service;

import com.xanqan.project.model.domain.User;
import com.xanqan.project.model.dto.File;
import com.xanqan.project.model.dto.Share;
import com.xanqan.project.model.vo.FileChunk;
import org.springframework.web.multipart.MultipartFile;

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
     * @param user 用户信息
     * @return 文件列表
     */
    List<File> getFileList(String path, User user);

    /**
     * 根据类型返回文件，分页
     *
     * @param type 文件类型
     * @param page 页码
     * @param rows 每页数量
     * @param user 用户信息
     * @return 文件列表
     */
    List<File> getFileListByType(String type, Integer page, Integer rows, User user);

    /**
     * 获取文件夹的总大小
     *
     * @param path       路径
     * @param folderName 文件夹名称
     * @param user       用户信息
     * @return 文件夹总大小（byte）
     */
    long getFolderSize(String path, String folderName, User user);

    /**
     * 创建文件夹
     *
     * @param path       路径
     * @param folderName 文件夹名称
     * @param user       用户信息
     * @return boolean
     */
    File createFolder(String path, String folderName, User user);

    /**
     * 删除文件夹及其下所有文件
     *
     * @param path       路径
     * @param folderName 文件夹名称
     * @param user       用户信息
     * @return boolean
     */
    boolean deleteFolder(String path, String folderName, User user);

    /**
     * 文件夹重命名
     *
     * @param path    文件的路径
     * @param oldName 原文件夹名
     * @param newName 修改后文件夹名
     * @param user    用户信息
     * @return 文件的可访问路径
     */
    boolean reNameFolder(String path, String oldName, String newName, User user);

    /**
     * 文件夹重命名
     *
     * @param oldPath    原文件夹路径
     * @param newPath    移动后文件夹路径
     * @param folderName 文件名
     * @param user       用户信息
     * @return 文件的可访问路径
     */
    boolean moveFolder(String oldPath, String newPath, String folderName, User user);

    /**
     * 文件夹重命名
     *
     * @param oldPath    原文件夹路径
     * @param newPath    复制后文件夹路径
     * @param folderName 文件名
     * @param user       用户信息
     * @return 文件的可访问路径
     */
    boolean copyFolder(String oldPath, String newPath, String folderName, User user);

    /**
     * 文件上传
     *
     * @param path          文件的路径
     * @param multipartFile 文件封装
     * @param user          用户信息
     * @return 文件的可访问路径
     */
    File upload(String path, MultipartFile multipartFile, User user);

    /**
     * 文件删除
     *
     * @param path     文件的路径
     * @param fileName 文件名
     * @param user     用户信息
     * @return 文件的可访问路径
     */
    boolean remove(String path, String fileName, User user);

    /**
     * 文件重命名
     *
     * @param path    文件的路径
     * @param oldName 原文件名
     * @param newName 修改后文件名
     * @param user    用户信息
     * @return 文件的可访问路径
     */
    boolean reName(String path, String oldName, String newName, User user);

    /**
     * 文件移动
     *
     * @param oldPath  原文件路径
     * @param newPath  移动后文件路径
     * @param fileName 文件名
     * @param user     用户信息
     * @return 文件的可访问路径
     */
    boolean move(String oldPath, String newPath, String fileName, User user);

    /**
     * 文件复制
     *
     * @param oldPath  原文件路径
     * @param newPath  复制后文件路径
     * @param fileName 文件名
     * @param user     用户信息
     * @return 文件的可访问路径
     */
    boolean copy(String oldPath, String newPath, String fileName, User user);

    /**
     * 初始化大文件上传
     *
     * @param path       文件路径
     * @param fileName   文件名
     * @param fileChunks 文件分片MD5
     * @param user       用户信息
     * @return 未上传的文件列表
     */
    List<FileChunk> initBigFileUpload(String path, String fileName, List<FileChunk> fileChunks, User user);

    /**
     * 初始化大文件上传
     *
     * @param path          文件路径
     * @param chunkId       分片id
     * @param multipartFile 文件封装
     * @param user          用户信息
     * @return 未上传的文件列表
     */
    File bigFileUpload(String path, String chunkId, MultipartFile multipartFile, User user);

    /**
     * 获取文件分享链接
     *
     * @param path     文件路径
     * @param fileName 文件名
     * @param password 分享密码
     * @param expire   超时时间（天）
     * @param user     用户信息
     * @return 文件的可访问路径
     */
    Share createShareUrl(String path, String fileName, String password, Integer expire, User user);


    /**
     * 获取文件分享链接
     *
     * @param id   分享id
     * @param user 用户信息
     * @return 文件的可访问路径
     */
    boolean removeShareUrl(String id, User user);

    /**
     * 获取用户全部分享链接
     *
     * @param user 用户信息
     * @return 文件的可访问路径
     */
    List<Share> getShareUrlAll(User user);

    /**
     * 下载分享
     *
     * @param shareId  分享id
     * @param password 分享密码
     * @return 文件的可访问路径
     */
    Share getShareUrl(String shareId, String password);

}
