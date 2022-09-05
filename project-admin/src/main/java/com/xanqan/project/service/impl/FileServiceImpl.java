package com.xanqan.project.service.impl;

import java.util.Date;

import com.mongodb.client.result.DeleteResult;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.security.util.JwtTokenUtil;
import com.xanqan.project.service.UserAdminService;
import com.xanqan.project.util.FileUtil;

import com.xanqan.project.model.dto.File;
import com.xanqan.project.service.FileService;
import com.xanqan.project.util.MinioUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 文件服务类
 *
 * @author xanqan
 */
@Service
public class FileServiceImpl implements FileService {

    @Resource
    private MinioUtil minioUtil;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private FileUtil fileUtil;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource(name = "userAdminServiceImpl")
    private UserAdminService userAdminService;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    /** 根目录 */
    public static final String ROOT_DIRECTORY = "/";

    private User getTokenUser(HttpServletRequest request) {
        String authHeader = request.getHeader(tokenHeader);
        String authToken = authHeader.substring(tokenHead.length());
        String userName = jwtTokenUtil.getUserNameFromToken(authToken);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", userName);
        return userAdminService.getOne(queryWrapper);
    }

    @Override
    public List<File> getFileList(String path, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        //用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = "xq" + user.getId().toString();

        Query query = Query.query(Criteria.where("path").is(path));

        return mongoTemplate.find(query, File.class, bucketName);
    }

    @Override
    public boolean createFolder(String path, String folderName, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(folderName ,path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        //用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = "xq" + user.getId().toString();

        //验证文件夹是否重复
        Query query = Query.query(Criteria.where("path").is(path)
                                    .and("name").is(folderName)
                                    .and("isFolder").is(1));
        long count = mongoTemplate.count(query, File.class, bucketName);
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件夹重复");
        }

        File file = new File();
        file.setName(folderName);
        file.setPath(path);
        file.setFileSize(0);
        file.setCreateTime(new Date());
        file.setModifyTime(new Date());
        file.setIsFolder(1);

        File result = mongoTemplate.insert(file, bucketName);

        return result.getId() != null;
    }

    @Override
    public boolean deleteFolder(String path, String folderName, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(folderName ,path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        //用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = "xq" + user.getId().toString();
        String folderPath = path + folderName;
        if (!ROOT_DIRECTORY.equals(path)) {
            folderPath = path + "/" + folderName;
        }

        Query queryFile = Query.query(Criteria.where("path").is(folderPath).and("isFolder").is(0));
        List<File> fileList = mongoTemplate.find(queryFile, File.class, bucketName);

        Query query = Query.query(Criteria.where("path").is(path).and("name").is(folderName).and("isFolder").is(1));
        DeleteResult result = mongoTemplate.remove(query, bucketName);
        mongoTemplate.remove(queryFile, bucketName);

        long deleteSizeNum = 0;

        //删除该文件夹下所有文件和计算其总大小
        for (File file : fileList) {
            deleteSizeNum += file.getFileSize();
            minioUtil.remove(bucketName, folderPath, file.getName());
        }

        user.setSizeUse(user.getSizeUse() - deleteSizeNum);
        userAdminService.updateById(user);

        return result.getDeletedCount() > 0;
    }

    @Override
    public String upload(String path, MultipartFile multipartFile, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (multipartFile.isEmpty() || multipartFile.getSize() <= 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件为空");
        }

        //用户信息获取
        User user = this.getTokenUser(request);

        String bucketName = "xq" + user.getId().toString();

        //验证文件夹是否存在
        if (!ROOT_DIRECTORY.equals(path)) {
            int i = path.lastIndexOf("/");
            Query query = Query.query(Criteria.where("path").is(path.substring(0, i + 1))
                                        .and("name").is(path.substring(i + 1))
                                        .and("isFolder").is(1));
            long count = mongoTemplate.count(query, File.class, bucketName);
            if (count <= 0) {
                throw new BusinessException(ResultCode.PARAMS_ERROR, "文件夹不存在");
            }
        }

        File file = fileUtil.read(path, multipartFile);

        //验证文件是否重复
        Query query = Query.query(Criteria.where("path").is(file.getPath())
                                    .and("name").is(file.getName())
                                    .and("isFolder").is(0));
        long count = mongoTemplate.count(query, File.class, bucketName);
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件重复");
        }

        //验证容量+增加容量
        if (file.getFileSize() + user.getSizeUse() > user.getSizeMax()) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "容量不足");
        }

        String result = minioUtil.upload(bucketName, path, multipartFile);
        mongoTemplate.insert(file, bucketName);
        user.setSizeUse(file.getFileSize() + user.getSizeUse());
        userAdminService.updateById(user);
        return result;
    }

    @Override
    public boolean remove(String path, String fileName, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(path, fileName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        //用户信息获取
        User user = this.getTokenUser(request);

        String bucketName = "xq" + user.getId().toString();

        minioUtil.remove(bucketName, path, fileName);

        Query query = Query.query(Criteria.where("path").is(path)
                                    .and("name").is(fileName)
                                    .and("isFolder").is(0));
        List<File> fileList = mongoTemplate.find(query, File.class, bucketName);
        mongoTemplate.remove(query, File.class, bucketName);
        user.setSizeUse(user.getSizeUse() - fileList.get(0).getFileSize());
        userAdminService.updateById(user);
        return true;
    }
}
