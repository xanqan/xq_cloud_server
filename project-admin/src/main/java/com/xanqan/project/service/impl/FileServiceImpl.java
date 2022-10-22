package com.xanqan.project.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mongodb.client.result.DeleteResult;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.model.dto.File;
import com.xanqan.project.service.FileService;
import com.xanqan.project.service.UserService;
import com.xanqan.project.util.FileUtil;
import com.xanqan.project.util.MinioUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文件服务类
 *
 * @author xanqan
 */
@Service
public class FileServiceImpl implements FileService {
    @Resource
    private UserService userService;
    @Resource
    private MinioUtil minioUtil;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private FileUtil fileUtil;

    /**
     * 根目录
     */
    private static final String ROOT_DIRECTORY = "/";
    /**
     * 存储桶处理
     */
    private static final String BUCKET_NAME_PREFIX = "xq";

    @Override
    public List<File> getFileList(String path, User user) {
        // 校验
        if (StrUtil.hasBlank(path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 获取路径下全部文件（包括文件夹）
        Query query = Query.query(Criteria.where("path").is(path));
        return mongoTemplate.find(query, File.class, bucketName);
    }

    @Override
    public long getFolderSize(String path, String folderName, User user) {
        // 校验
        if (StrUtil.hasBlank(folderName, path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 文件夹路径加工
        String folderPath = this.folderPathProcess(path, folderName);

        // 递归搜索路径下的所有文件
        Pattern pattern = Pattern.compile("^" + folderPath.replace("/", "\\/") + "\\/" + "(.*)");
        Query query = Query.query(new Criteria()
                .orOperator(Criteria.where("path").is(folderPath), Criteria.where("path").regex(pattern))
                .and("isFolder").is(0));
        List<File> files = mongoTemplate.find(query, File.class, bucketName);

        // 计算其总大小
        return files.stream().mapToLong(File::getFileSize).sum();
    }

    @Override
    public File createFolder(String path, String folderName, User user) {
        // 校验
        if (StrUtil.hasBlank(folderName, path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

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

        return mongoTemplate.insert(file, bucketName);
    }

    @Override
    public boolean deleteFolder(String path, String folderName, User user) {
        // 校验
        if (StrUtil.hasBlank(folderName, path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 文件夹路径加工
        String folderPath = this.folderPathProcess(path, folderName);

        // 验证文件夹是否存在
        this.isFolderExist(bucketName, folderPath);

        Pattern pattern = Pattern.compile("^" + folderPath.replace("/", "\\/") + "\\/" + "(.*)");
        // 递归搜索该文件夹下所有文件并用于后面操作
        Query queryFile = Query.query(new Criteria()
                .orOperator(Criteria.where("path").is(folderPath), Criteria.where("path").regex(pattern))
                .and("isFolder").is(0));
        List<File> files = mongoTemplate.find(queryFile, File.class, bucketName);

        // 递归搜索删除该文件夹下所有内容(包括该文件夹)
        Query query = Query.query(new Criteria()
                .orOperator(Criteria.where("path").is(folderPath), Criteria.where("path").regex(pattern),
                        Criteria.where("path").is(path).and("name").is(folderName).and("isFolder").is(1)));
        DeleteResult result = mongoTemplate.remove(query, bucketName);

        // 删除该文件夹下所有文件
        boolean removeBatch = minioUtil.removeBatch(bucketName,
                files.stream().map(File::getPath).collect(Collectors.toList()),
                files.stream().map(File::getName).collect(Collectors.toList()));

        // 更新用户容量
        user.setSizeUse(user.getSizeUse() - files.stream().mapToLong(File::getFileSize).sum());
        boolean updateById = userService.updateById(user);

        return result.getDeletedCount() > 0 && removeBatch && updateById;
    }

    @Override
    public boolean reNameFolder(String path, String oldName, String newName, User user) {
        // 校验
        if (StrUtil.hasBlank(path, oldName, newName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (oldName.equals(newName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "前后文件夹名一样");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 文件夹路径加工
        String oldFolderPath = this.folderPathProcess(path, oldName);
        String newFolderPath = this.folderPathProcess(path, newName);

        // 验证文件夹是否存在,以及是否重复
        this.isFolderExist(bucketName, oldFolderPath);
        this.isFolderRepeat(bucketName, path, newName);

        Pattern pattern = Pattern.compile("^" + oldFolderPath.replace("/", "\\/") + "\\/" + "(.*)");
        // 递归搜索该文件夹下所有内容后续更新
        Query queryAll = Query.query(new Criteria()
                .orOperator(Criteria.where("path").is(oldFolderPath), Criteria.where("path").regex(pattern)));
        List<File> list = mongoTemplate.find(queryAll, File.class, bucketName);

        // 更新该文件夹下所有文件（包括文件夹）的路径
        Map<String, String> map = new HashMap<>(list.size());
        for (File file : list) {
            String filePath = file.getPath();
            int i = filePath.indexOf(oldFolderPath) + oldFolderPath.length();
            file.setPath(newFolderPath + filePath.substring(i));
            map.put(filePath, file.getPath());
            if (file.getIsFolder() == 0) {
                minioUtil.move(bucketName, filePath, file.getPath(), file.getName(), file.getName());
            }
        }
        for (String oldPath : map.keySet()) {
            String newPath = map.get(oldPath);
            Query query = Query.query(Criteria.where("path").is(oldPath));
            Update update = Update.update("path", newPath);
            mongoTemplate.updateMulti(query, update, bucketName);
        }

        // 更新该文件夹
        Query queryFolder = Query.query(Criteria.where("path").is(path).and("name").is(oldName).and("isFolder").is(1));
        Update updateFolder = Update.update("name", newName);
        mongoTemplate.updateFirst(queryFolder, updateFolder, bucketName);

        return true;
    }

    @Override
    public File upload(String path, MultipartFile multipartFile, User user) {
        // 校验
        if (StrUtil.hasBlank(path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (multipartFile.isEmpty() || multipartFile.getSize() <= 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件为空");
        }

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
        minioUtil.upload(bucketName, path, multipartFile);
        mongoTemplate.insert(file, bucketName);
        user.setSizeUse(file.getFileSize() + user.getSizeUse());
        userService.updateById(user);

        return file;
    }

    @Override
    public boolean remove(String path, String fileName, User user) {
        // 校验
        if (StrUtil.hasBlank(path, fileName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 查询要删除文件的详细信息
        Query query = Query.query(Criteria.where("path").is(path)
                .and("name").is(fileName)
                .and("isFolder").is(0));
        List<File> files = mongoTemplate.find(query, File.class, bucketName);

        // 删除文件, 更新容量
        minioUtil.remove(bucketName, path, fileName);
        mongoTemplate.remove(query, File.class, bucketName);
        user.setSizeUse(user.getSizeUse() - files.get(0).getFileSize());
        userService.updateById(user);

        return true;
    }

    @Override
    public boolean reName(String path, String oldName, String newName, User user) {
        // 校验
        if (StrUtil.hasBlank(path, oldName, newName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (oldName.equals(newName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "前后文件名一样");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 验证文件是否存在或重复
        this.isExist(bucketName, path, oldName);
        this.isRepeat(bucketName, path, newName);

        //更新文件
        Query query = Query.query(Criteria.where("path").is(path)
                .and("name").is(oldName)
                .and("isFolder").is(0));
        Update update = Update.update("name", newName);
        mongoTemplate.updateFirst(query, update, bucketName);
        minioUtil.move(bucketName, path, path, oldName, newName);

        return true;
    }

    @Override
    public boolean move(String oldPath, String newPath, String fileName, User user) {
        // 校验
        if (StrUtil.hasBlank(oldPath, newPath, fileName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (oldPath.equals(newPath)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "前后文件名一样");
        }

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
        mongoTemplate.updateFirst(query, update, bucketName);
        minioUtil.move(bucketName, oldPath, newPath, fileName, fileName);

        return true;
    }

    /**
     * 文件夹路径加工，用于后续遍历
     *
     * @param path       文件夹路径
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
     * @param path       文件路径
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
     * @param path       文件夹路径
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
     * 验证文件是否存在
     *
     * @param bucketName 存储桶名
     * @param path       文件路径
     */
    private void isExist(String bucketName, String path, String fileName) {
        Query queryCount = Query.query(Criteria.where("path").is(path)
                .and("name").is(fileName)
                .and("isFolder").is(0));
        long count = mongoTemplate.count(queryCount, File.class, bucketName);
        if (count <= 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件重复");
        }
    }

    /**
     * 验证文件是否重复
     *
     * @param bucketName 存储桶名
     * @param path       文件路径
     * @param fileName   文件名
     */
    private void isRepeat(String bucketName, String path, String fileName) {
        Query queryCount = Query.query(Criteria.where("path").is(path)
                .and("name").is(fileName)
                .and("isFolder").is(0));
        long count = mongoTemplate.count(queryCount, File.class, bucketName);
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件重复");
        }
    }
}