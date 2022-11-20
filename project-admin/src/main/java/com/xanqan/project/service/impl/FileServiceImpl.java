package com.xanqan.project.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.mongodb.client.result.DeleteResult;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.model.dto.File;
import com.xanqan.project.model.dto.Share;
import com.xanqan.project.model.vo.FileChunk;
import com.xanqan.project.service.FileService;
import com.xanqan.project.service.RedisService;
import com.xanqan.project.service.UserService;
import com.xanqan.project.util.FileUtil;
import com.xanqan.project.util.MinioUtil;
import io.minio.GetObjectResponse;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

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
    @Resource
    private RedisService redisService;

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
    public List<File> getFileListByType(String type, Integer page, Integer rows, User user) {
        // 校验
        if (StrUtil.hasBlank(type)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (page < 1 && rows < 1) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为负");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        Pageable pageable = PageRequest.of(page - 1, rows, Sort.by(Sort.Order.desc("name")));
        Criteria criteria = Criteria.where("isFolder").is(0);
        if ("photo".equals(type)) {
            criteria.andOperator(Criteria.where("type").is("photo"));
        } else if ("video".equals(type)) {
            criteria.andOperator(Criteria.where("type").is("video"));
        } else if ("audio".equals(type)) {
            criteria.andOperator(Criteria.where("type").is("audio"));
        } else if ("text".equals(type)) {
            criteria.andOperator(Criteria.where("type").is("text"));
        }
        Query query = Query.query(criteria).with(pageable);

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
        for (File file : files) {
            minioUtil.remove(bucketName, file.getPath(), file.getName());
        }

        // 更新用户容量
        long sizeUse = user.getSizeUse() - files.stream().mapToLong(File::getFileSize).sum();
        user.setSizeUse(sizeUse > 0 ? sizeUse : 0);
        boolean updateById = userService.updateById(user);

        return result.getDeletedCount() > 0 && updateById;
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
        // 递归搜索该文件夹下所有内容
        Query queryAll = Query.query(new Criteria()
                .orOperator(Criteria.where("path").is(oldFolderPath), Criteria.where("path").regex(pattern)));
        List<File> files = mongoTemplate.find(queryAll, File.class, bucketName);

        // 更新该文件夹下所有文件（包括文件夹）的路径
        Map<String, String> map = new HashMap<>(files.size());
        for (File file : files) {
            String filePath = file.getPath();
            int i = filePath.indexOf(oldFolderPath) + oldFolderPath.length();
            file.setPath(newFolderPath + filePath.substring(i));
            map.put(filePath, file.getPath());
            if (file.getIsFolder() == 0) {
                minioUtil.move(bucketName, filePath, file.getPath(), file.getName(), file.getName());
            }
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Query query = Query.query(Criteria.where("path").is(entry.getKey()));
            Update update = Update.update("path", entry.getValue());
            mongoTemplate.updateMulti(query, update, bucketName);
        }

        // 更新该文件夹
        Query queryFolder = Query.query(Criteria.where("path").is(path).and("name").is(oldName).and("isFolder").is(1));
        Update updateFolder = new Update()
                .set("name", newName)
                .set("modifyTime", new Date());
        mongoTemplate.updateFirst(queryFolder, updateFolder, bucketName);

        return true;
    }

    @Override
    public boolean moveFolder(String oldPath, String newPath, String folderName, User user) {
        // 校验
        if (StrUtil.hasBlank(oldPath, newPath, folderName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (oldPath.equals(newPath)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "前后路径一样");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 文件夹路径加工
        String oldFolderPath = this.folderPathProcess(oldPath, folderName);
        String newFolderPath = this.folderPathProcess(newPath, folderName);

        // 验证文件夹是否存在,以及是否重复
        this.isFolderExist(bucketName, oldFolderPath);
        this.isFolderRepeat(bucketName, newPath, folderName);

        Pattern pattern = Pattern.compile("^" + oldFolderPath.replace("/", "\\/") + "\\/" + "(.*)");
        // 递归搜索该文件夹下所有内容
        Query queryAll = Query.query(new Criteria()
                .orOperator(Criteria.where("path").is(oldFolderPath), Criteria.where("path").regex(pattern)));
        List<File> files = mongoTemplate.find(queryAll, File.class, bucketName);

        // 更新该文件夹下所有文件（包括文件夹）的路径
        Map<String, String> map = new HashMap<>(files.size());
        for (File file : files) {
            String filePath = file.getPath();
            int i = filePath.indexOf(oldFolderPath) + oldFolderPath.length();
            file.setPath(newFolderPath + filePath.substring(i));
            map.put(filePath, file.getPath());
            if (file.getIsFolder() == 0) {
                minioUtil.move(bucketName, filePath, file.getPath(), file.getName(), file.getName());
            }
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Query query = Query.query(Criteria.where("path").is(entry.getKey()));
            Update update = Update.update("path", entry.getValue());
            mongoTemplate.updateMulti(query, update, bucketName);
        }

        // 更新该文件夹
        Query queryFolder = Query.query(Criteria.where("path").is(oldPath).and("name").is(folderName).and("isFolder").is(1));
        Update updateFolder = new Update()
                .set("path", newPath)
                .set("modifyTime", new Date());
        mongoTemplate.updateFirst(queryFolder, updateFolder, bucketName);

        return true;
    }

    @Override
    public boolean copyFolder(String oldPath, String newPath, String folderName, User user) {
        // 校验
        if (StrUtil.hasBlank(oldPath, newPath, folderName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (oldPath.equals(newPath)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "前后路径一样");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 文件夹路径加工
        String oldFolderPath = this.folderPathProcess(oldPath, folderName);
        String newFolderPath = this.folderPathProcess(newPath, folderName);

        // 验证文件夹是否存在,以及是否重复
        this.isFolderExist(bucketName, oldFolderPath);
        this.isFolderRepeat(bucketName, newPath, folderName);

        Pattern pattern = Pattern.compile("^" + oldFolderPath.replace("/", "\\/") + "\\/" + "(.*)");
        // 递归搜索该文件夹下所有内容
        Query queryAll = Query.query(new Criteria()
                .orOperator(Criteria.where("path").is(oldFolderPath), Criteria.where("path").regex(pattern)));
        List<File> files = mongoTemplate.find(queryAll, File.class, bucketName);

        // 更新该文件夹下所有文件（包括文件夹）的路径
        for (File file : files) {
            String filePath = file.getPath();
            int i = filePath.indexOf(oldFolderPath) + oldFolderPath.length();
            file.setPath(newFolderPath + filePath.substring(i));
            file.setId(null);
            if (file.getIsFolder() == 0) {
                minioUtil.copy(bucketName, filePath, file.getPath(), file.getName(), file.getName());
            }
            mongoTemplate.insert(file, bucketName);
        }

        // 更新该文件夹
        Query queryFolder = Query.query(Criteria.where("path").is(oldPath).and("name").is(folderName).and("isFolder").is(1));
        File file = mongoTemplate.find(queryFolder, File.class, bucketName).get(0);
        file.setId(null);
        file.setPath(newPath);
        file.setModifyTime(new Date());
        mongoTemplate.insert(file, bucketName);

        // 更新用户容量
        long sizeUse = user.getSizeUse() + files.stream().mapToLong(File::getFileSize).sum();
        user.setSizeUse(sizeUse > 0 ? sizeUse : 0);
        return userService.updateById(user);
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
        File insert = mongoTemplate.insert(file, bucketName);
        user.setSizeUse(file.getFileSize() + user.getSizeUse());
        minioUtil.upload(bucketName, insert, multipartFile);
        userService.updateById(user);

        //图片文件生成缩略图
        InputStream thumbnailIn = null;
        if ("photo".equals(file.getType())) {
            try {
                BufferedImage bufferedImage = Thumbnails.of(multipartFile.getInputStream()).size(200, 200).asBufferedImage();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", os);
                thumbnailIn = new ByteArrayInputStream(os.toByteArray());
                minioUtil.uploadImg(bucketName, insert.getId().toString(), thumbnailIn);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (thumbnailIn != null) {
                        thumbnailIn.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

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
        File file = mongoTemplate.find(query, File.class, bucketName).get(0);

        // 删除文件, 更新容量
        minioUtil.remove(bucketName, path, fileName);
        mongoTemplate.remove(query, File.class, bucketName);
        user.setSizeUse(user.getSizeUse() - file.getFileSize());
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
        Update update = new Update()
                .set("name", newName)
                .set("type", fileUtil.findType(fileUtil.findContentType(newName)))
                .set("contentType", fileUtil.findContentType(newName))
                .set("modifyTime", new Date());
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
            throw new BusinessException(ResultCode.PARAMS_ERROR, "前后路径一样");
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

    @Override
    public boolean copy(String oldPath, String newPath, String fileName, User user) {
        // 校验
        if (StrUtil.hasBlank(oldPath, newPath, fileName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (oldPath.equals(newPath)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "前后路径一样");
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
        File file = mongoTemplate.find(query, File.class, bucketName).get(0);
        file.setId(null);
        file.setPath(newPath);
        mongoTemplate.insert(file, bucketName);
        minioUtil.copy(bucketName, oldPath, newPath, fileName, fileName);

        long sizeUse = user.getSizeUse() + file.getFileSize();
        user.setSizeUse(sizeUse > 0 ? sizeUse : 0);
        return userService.updateById(user);
    }

    @Override
    public List<FileChunk> initBigFileUpload(String path, String fileName, List<FileChunk> fileChunks, User user) {
        if (StrUtil.hasBlank(path, fileName) || fileChunks.size() <= 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        // 验证文件夹是否存在
        this.isFolderExist(bucketName, path);

        // 验证文件是否重复
        this.isRepeat(bucketName, path, fileName);

        String key = this.folderPathProcess(path, fileName);
        Map<Object, Object> map = redisService.getHash(key);
        if (map.size() <= 0) {
            for (FileChunk fileChunk : fileChunks) {
                map.put(fileChunk.getId().toString(), fileChunk.getMd5());
            }
            redisService.setHash(key, map);
        } else {
            fileChunks.clear();
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                FileChunk fileChunk = new FileChunk();
                fileChunk.setId(Integer.parseInt((String) entry.getKey()));
                fileChunk.setMd5((String) entry.getValue());
                fileChunks.add(fileChunk);
            }
        }
        return fileChunks;
    }

    @Override
    public File bigFileUpload(String path, String chunkId, MultipartFile multipartFile, User user) {
        // 校验
        if (StrUtil.hasBlank(path, chunkId)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (multipartFile.isEmpty() || multipartFile.getSize() <= 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件为空");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();

        File file = new File();
        file.setName(chunkId);
        file.setPath(path + "_chunk");

        Map<Object, Object> map = redisService.getHash(path);
        if (map.size() <= 0) {
            throw new BusinessException(ResultCode.FAILED, "没有该文件分片信息");
        } else if (!map.containsKey(chunkId)) {
            throw new BusinessException(ResultCode.FAILED, "没有该分片信息或者已经上传完成");
        } else {
            InputStream in = null;
            String md5 = null;
            try {
                in = multipartFile.getInputStream();
                md5 = SecureUtil.md5(in);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!map.get(chunkId).equals(md5)) {
                throw new BusinessException(ResultCode.FAILED, "分片校验不通过");
            }
            minioUtil.upload(bucketName, file, multipartFile);
            redisService.removeHashKey(path, chunkId);

            if (map.size() <= 1) {
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                path = path.substring(0, path.lastIndexOf("/"));
                if ("".equals(path)) {
                    path = ROOT_DIRECTORY;
                }
                minioUtil.compose(bucketName, path, fileName);
                GetObjectResponse getObjectResponse = minioUtil.getFile(bucketName, path, fileName);
                File fileCompose = new File();
                fileCompose.setName(fileName);
                fileCompose.setPath(path);
                fileCompose.setFileSize(Long.parseLong(Objects.requireNonNull(getObjectResponse.headers().get("Content-Length"))));
                fileCompose.setContentType(fileUtil.findContentType(fileName));
                fileCompose.setType(fileUtil.findType(fileCompose.getContentType()));
                fileCompose.setCreateTime(new Date());
                fileCompose.setModifyTime(new Date());
                fileCompose.setIsFolder(0);
                BufferedImage bufferedImage = null;
                if ("photo".equals(fileCompose.getType())) {
                    try {
                        bufferedImage = ImageIO.read(getObjectResponse);
                        fileCompose.setSize(bufferedImage.getWidth() + "*" + bufferedImage.getHeight());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // 验证容量
                if (fileCompose.getFileSize() + user.getSizeUse() > user.getSizeMax()) {
                    throw new BusinessException(ResultCode.PARAMS_ERROR, "容量不足");
                }

                // 上传文件, 更新容量
                File insert = mongoTemplate.insert(fileCompose, bucketName);
                user.setSizeUse(fileCompose.getFileSize() + user.getSizeUse());
                userService.updateById(user);

                //图片文件生成缩略图
                InputStream thumbnailIn = null;
                if ("photo".equals(fileCompose.getType())) {
                    try {
                        BufferedImage thumbnail = Thumbnails.of(bufferedImage).size(200, 200).asBufferedImage();
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageIO.write(thumbnail, "jpg", os);
                        thumbnailIn = new ByteArrayInputStream(os.toByteArray());
                        minioUtil.uploadImg(bucketName, insert.getId().toString(), thumbnailIn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (thumbnailIn != null) {
                                thumbnailIn.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return fileCompose;
            }
            return null;
        }
    }

    @Override
    public Share createShareUrl(String path, String fileName, String password, Integer expire, User user) {
        // 校验
        if (StrUtil.hasBlank(path, fileName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (expire == null || expire < 1) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "过期时间错误");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();
        String shareName = bucketName.concat("Share");

        // 验证文件是否存在
        this.isExist(bucketName, path, fileName);

        String shareUrl = minioUtil.getPresignedObjectUrl(bucketName, path, fileName, expire);

        Share share = new Share();
        share.setUrl(shareUrl);
        share.setPassword(password);
        share.setName(fileName);
        share.setPath(path);
        share.setType(fileUtil.findType(fileUtil.findContentType(fileName)));
        share.setExpire(new Date(System.currentTimeMillis() + expire * 24 * 60 * 60 * 1000L));

        return mongoTemplate.insert(share, shareName);
    }

    @Override
    public boolean removeShareUrl(String id, User user) {
        // 校验
        if (StrUtil.hasBlank(id)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }

        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();
        String shareName = bucketName.concat("Share");

        Query query = Query.query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, shareName);
        return true;
    }

    @Override
    public List<Share> getShareUrlAll(User user) {
        String bucketName = BUCKET_NAME_PREFIX + user.getId().toString();
        String shareName = bucketName.concat("Share");
        List<Share> shares = mongoTemplate.findAll(Share.class, shareName);
        Date date = new Date();
        for (Share share : shares) {
            if (date.after(share.getExpire())) {
                Query query = Query.query(Criteria.where("_id").is(share.getId()));
                mongoTemplate.remove(query, shareName);
            }
        }
        return mongoTemplate.findAll(Share.class, shareName);
    }

    @Override
    public Share getShareUrl(String shareId, String password) {
        // 校验
        if (StrUtil.hasBlank(shareId)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        int index = shareId.lastIndexOf(".");
        String bucketName = shareId.substring(0, index);
        String shareName = bucketName.concat("Share");
        String id = shareId.substring(index + 1);
        Query query = Query.query(Criteria.where("_id").is(id));
        List<Share> shares = mongoTemplate.findAll(Share.class, shareName);
        Share share = null;
        for (Share value : shares) {
            if (value.getId().equals(id)) {
                share = value;
                break;
            }
        }
        if (share == null) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "没有该分享");
        }
        Date date = new Date();
        if (date.after(share.getExpire())) {
            mongoTemplate.remove(query, shareName);
            throw new BusinessException(ResultCode.FAILED, "该分享已到期");
        }
        return share;
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
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件不存在");
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