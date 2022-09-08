package com.xanqan.project.service.impl;

import java.util.*;

import com.mongodb.client.result.DeleteResult;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mongodb.client.result.UpdateResult;
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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

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
    @Resource
    private UserAdminService userAdminService;

    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    /** 根目录 */
    private static final String ROOT_DIRECTORY = "/";
    /** 根目录 */
    private static final String BUCKET_NAME_PREFIX = "xq";

    @Override
    public List<File> getFileList(String path, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        // 用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 获取路径下全部文件（包括文件夹）
        Query query = Query.query(Criteria.where("path").is(path));
        return mongoTemplate.find(query, File.class, bucketName);
    }

    @Override
    public long getFolderSize(String path, String folderName, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(folderName ,path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        // 用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 文件夹路径加工
        String folderPath = this.folderPathProcess(path, folderName);

        // 递归搜索路径下的所有文件
        Criteria criteria = new Criteria();
        Pattern pattern = Pattern.compile("^" + folderPath.replace("/", "\\/") + "\\/" + "(.*)");
        criteria.orOperator(Criteria.where("path").is(folderPath), Criteria.where("path").regex(pattern))
                .and("isFolder").is(0);
        Query query = Query.query(criteria);
        List<File> fileList = mongoTemplate.find(query, File.class, bucketName);

        // 计算其总大小
        long sizeNum = 0;
        for (File file : fileList) {
            sizeNum += file.getFileSize();
        }

        return sizeNum;
    }

    @Override
    public boolean createFolder(String path, String folderName, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(folderName ,path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        // 用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 验证文件夹是否重复
        this.isFolderRepeat(bucketName, path, folderName);

        // 新建文件夹
        File file = new File();
        file.setName(folderName);
        file.setPath(path);
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

        // 用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 文件夹路径加工
        String folderPath = this.folderPathProcess(path, folderName);

        // 验证文件夹是否存在
        this.isFolderExist(bucketName, folderPath);

        Pattern pattern = Pattern.compile("^" + folderPath.replace("/", "\\/") + "\\/" + "(.*)");
        // 递归搜索该文件夹下所有文件并用于后面操作
        Criteria criteriaFile = new Criteria();
        criteriaFile.orOperator(Criteria.where("path").is(folderPath), Criteria.where("path").regex(pattern))
                .and("isFolder").is(0);
        Query queryFile = Query.query(criteriaFile);
        List<File> fileList = mongoTemplate.find(queryFile, File.class, bucketName);

        // 递归搜索删除该文件夹下所有内容(包括该文件夹)
        Criteria criteria = new Criteria();
        criteria.orOperator(Criteria.where("path").is(folderPath), Criteria.where("path").regex(pattern),
                Criteria.where("path").is(path).and("name").is(folderName).and("isFolder").is(1));
        Query query = Query.query(criteria);
        DeleteResult result = mongoTemplate.remove(query, bucketName);

        // 删除该文件夹下所有文件和计算其总大小
        long deleteSizeNum = 0;
        for (File file : fileList) {
            deleteSizeNum += file.getFileSize();
            minioUtil.remove(bucketName, folderPath, file.getName());
        }

        // 更新用户容量
        user.setSizeUse(user.getSizeUse() - deleteSizeNum);
        userAdminService.updateById(user);

        return result.getDeletedCount() > 0;
    }

    @Override
    public String reNameFolder(String path, String oldName, String newName, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(path, oldName, newName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (oldName.equals(newName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件夹名一样");
        }

        // 用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 文件夹路径加工
        String oldFolderPath = this.folderPathProcess(path, oldName);
        String newFolderPath = this.folderPathProcess(path, newName);

        // 验证文件夹是否存在,以及是否重复
        this.isFolderExist(bucketName, oldFolderPath);
        this.isFolderRepeat(bucketName, path, newName);

        Pattern pattern = Pattern.compile("^" + oldFolderPath.replace("/", "\\/") + "\\/" + "(.*)");
        // 递归搜索该文件夹下所有内容后续更新
        Criteria criteriaAll = new Criteria();
        criteriaAll.orOperator(Criteria.where("path").is(oldFolderPath), Criteria.where("path").regex(pattern));
        Query queryAll = Query.query(criteriaAll);
        List<File> list = mongoTemplate.find(queryAll, File.class, bucketName);

        // 更新该文件夹所有文件的路径
        Map<String, String> map = new HashMap<>(list.size());
        for (File file : list) {
            String filePath = file.getPath();
            int i = filePath.indexOf(oldFolderPath) + oldFolderPath.length();
            file.setPath(newFolderPath + filePath.substring(i));
            map.put(filePath, file.getPath());
            if (file.getIsFolder() == 0) {
                minioUtil.copy(bucketName, filePath , file.getPath(), file.getName(), file.getName());
            }
        }
        for (String oldPath : map.keySet()) {
            String newPath = map.get(oldPath);
            Query query = Query.query(Criteria.where("path").is(oldPath));
            Update update = Update.update("path", newPath);
            mongoTemplate.updateMulti(query, update, bucketName);
        }

        // 更新该文件夹
        Query queryFolder = Query.query(Criteria.where("path").is(path).and("name").is(oldName));
        Update updateFolder = Update.update("name", newName);
        UpdateResult result = mongoTemplate.updateFirst(queryFolder, updateFolder, bucketName);

        return result.toString();
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

        // 用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 验证文件夹是否存在
        this.isFolderExist(bucketName, path);

        // 文件信息处理
        File file = fileUtil.read(path, multipartFile);

        // 验证文件是否重复
        this.isRepeat(bucketName, file.getPath(), file.getName());

        // 验证容量
        if (file.getFileSize() + user.getSizeUse() > user.getSizeMax()) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "容量不足");
        }

        // 上传文件, 更新容量
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

        // 用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 查询要删除文件的详细信息
        Query query = Query.query(Criteria.where("path").is(path)
                .and("name").is(fileName)
                .and("isFolder").is(0));
        List<File> fileList = mongoTemplate.find(query, File.class, bucketName);

        // 删除文件, 更新容量
        minioUtil.remove(bucketName, path, fileName);
        mongoTemplate.remove(query, File.class, bucketName);
        user.setSizeUse(user.getSizeUse() - fileList.get(0).getFileSize());
        userAdminService.updateById(user);
        return true;
    }

    @Override
    public String reName(String path, String oldName, String newName, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(path, oldName, newName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (oldName.equals(newName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件名一样");
        }

        // 用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 验证文件是否重复
        this.isRepeat(bucketName, path, newName);

        //更新文件
        Query query = Query.query(Criteria.where("path").is(path)
                .and("name").is(oldName)
                .and("isFolder").is(0));
        Update update = Update.update("name", newName);

        String result = minioUtil.copy(bucketName, path, path, oldName, newName);
        mongoTemplate.updateFirst(query, update, bucketName);
        return result;
    }

    @Override
    public String move(String oldPath, String newPath, String fileName, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(oldPath, newPath, fileName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (oldPath.equals(newPath)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件名一样");
        }

        // 用户信息获取
        User user = this.getTokenUser(request);
        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 验证文件夹是否存在
        this.isFolderExist(bucketName, newPath);

        // 验证文件是否重复
        this.isRepeat(bucketName, newPath, fileName);

        //更新文件
        Query query = Query.query(Criteria.where("path").is(oldPath)
                .and("name").is(fileName)
                .and("isFolder").is(0));
        Update update = Update.update("path", newPath);

        String result = minioUtil.copy(bucketName, oldPath, newPath, fileName, fileName);
        mongoTemplate.updateFirst(query, update, bucketName);
        return result;
    }

    /**
     * 由 request 中取出 token 再取出 name 再根据 name 从数据库中取出用户详细信息
     *
     * @param request 用于获取 token
     * @return user
     */
    private User getTokenUser(HttpServletRequest request) {
        String authHeader = request.getHeader(tokenHeader);
        String authToken = authHeader.substring(tokenHead.length());
        String userName = jwtTokenUtil.getUserNameFromToken(authToken);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", userName);
        return userAdminService.getOne(queryWrapper);
    }

    /**
     * 文件夹路径加工，用于后续遍历
     *
     * @param path 文件夹路径
     * @param folderName 文件夹名
     * @return 加工后路径
     */
    private String folderPathProcess(String path, String folderName) {
        String folderPath = path + folderName;
        if (!ROOT_DIRECTORY.equals(path)) {
            folderPath = path + "/" + folderName;
        }
        return folderPath;
    }

    /**
     * 验证文件夹是否存在
     *
     * @param bucketName 存储桶名
     * @param path 文件路径
     */
    private void isFolderExist(String bucketName, String path) {
        if (!ROOT_DIRECTORY.equals(path)) {
            int i = path.lastIndexOf("/");
            Query query = Query.query(Criteria.where("path").is(i == 0 ? ROOT_DIRECTORY : path.substring(0, i))
                    .and("name").is(path.substring(i + 1))
                    .and("isFolder").is(1));
            long count = mongoTemplate.count(query, File.class, bucketName);
            if (count <= 0) {
                throw new BusinessException(ResultCode.PARAMS_ERROR, "文件夹不存在");
            }
        }
    }

    /**
     * 验证文件夹是否重复
     *
     * @param bucketName 存储桶名
     * @param path 文件夹路径
     * @param folderName 文件夹名
     */
    private void isFolderRepeat(String bucketName, String path, String folderName) {
        Query query = Query.query(Criteria.where("path").is(path)
                .and("name").is(folderName)
                .and("isFolder").is(1));
        long count = mongoTemplate.count(query, File.class, bucketName);
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件夹重复");
        }
    }

    /**
     * 验证文件是否重复
     *
     * @param bucketName 存储桶名
     * @param path 文件路径
     * @param fileName 文件名
     */
    private void isRepeat(String bucketName, String path, String fileName) {
        Query queryCount = Query.query(Criteria.where("path").is(path)
                .and("name").is(fileName)
                .and("isFolder").is(0));
        long count = mongoTemplate.count(queryCount, File.class, bucketName);
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件名重复");
        }
    }
}