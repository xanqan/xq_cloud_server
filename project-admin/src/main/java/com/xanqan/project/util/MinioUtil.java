package com.xanqan.project.util;

import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import io.minio.*;
import io.minio.messages.DeleteObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * MinIO 服务封装工具类
 *
 * @author xanqan
 */
@Component
public class MinioUtil {
    @Resource
    private MinioClient minioClient;

    @Value("${minio.url}")
    private String url;
    /**
     * 根目录
     */
    private static final String ROOT_DIRECTORY = "/";

    /**
     * 判断存储桶是否存在，没有则创建并赋予访问规则
     *
     * @param bucketName 存储桶名
     */
    public void existBucket(String bucketName) {
        try {
            boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exist) {
                this.makeBucket(bucketName);
                String policy = "{\n" +
                        "    \"Version\": \"2012-10-17\",\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": {\n" +
                        "                \"AWS\": [\n" +
                        "                    \"*\"\n" +
                        "                ]\n" +
                        "            },\n" +
                        "            \"Action\": [\n" +
                        "                \"s3:GetObject\"\n" +
                        "            ],\n" +
                        "            \"Resource\": [\n" +
                        "                \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}";
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
            }
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名
     */
    public void makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 删除存储桶
     *
     * @param bucketName 存储桶名
     * @return boolean
     */
    public boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
        return true;
    }

    /**
     * 文件上传
     *
     * @param bucketName    存储桶名
     * @param path          文件路径
     * @param multipartFile 文件封装
     * @return 文件的可访问路径
     */
    public String upload(String bucketName, String path, MultipartFile multipartFile) {
        existBucket(bucketName);
        String object = path + "/" + multipartFile.getOriginalFilename();
        InputStream in = null;
        try {
            in = multipartFile.getInputStream();
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(object)
                    .stream(in, in.available(), -1)
                    .contentType(multipartFile.getContentType())
                    .build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return url + "/" + bucketName + object;
    }

    /**
     * 文件删除
     *
     * @param bucketName 存储桶名
     * @param path       文件路径
     * @param fileName   文件名
     * @return boolean
     */
    public boolean remove(String bucketName, String path, String fileName) {
        String object = path + "/" + fileName;
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(object).build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
        return true;
    }

    /**
     * 文件删除
     *
     * @param bucketName 存储桶名
     * @param paths      文件路径集合
     * @param fileNames  文件名集合
     * @return boolean
     */
    public boolean removeBatch(String bucketName, List<String> paths, List<String> fileNames) {
        List<DeleteObject> objects = new ArrayList<>();
        int n = paths.size();
        for (int i = 0; i < n; ++i) {
            objects.add(new DeleteObject(paths.get(i).concat("/").concat(fileNames.get(i))));
        }
        try {
            minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
        return true;
    }

    /**
     * 文件移动
     *
     * @param bucketName 存储桶名
     * @param oldPath    原路径
     * @param newPath    修改后路径
     * @param oldName    原文件名
     * @param newName    修改后文件名
     * @return 文件的可访问路径
     */
    public String move(String bucketName, String oldPath, String newPath, String oldName, String newName) {
        String oldObject = oldPath.concat(oldName);
        if (!ROOT_DIRECTORY.equals(oldPath)) {
            oldObject = oldPath.concat(ROOT_DIRECTORY).concat(oldName);
        }
        String newObject = newPath.concat(newName);
        if (!ROOT_DIRECTORY.equals(newPath)) {
            newObject = newPath.concat(ROOT_DIRECTORY).concat(newName);
        }
        try {
            CopySource copySource = CopySource.builder().bucket(bucketName).object(oldObject).build();
            minioClient.copyObject(CopyObjectArgs.builder().bucket(bucketName).object(newObject).source(copySource).build());
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(oldObject).build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
        return url + "/" + bucketName + newObject;
    }

    /**
     * 文件拷贝
     *
     * @param bucketName 存储桶名
     * @param oldPath    原路径
     * @param newPath    修改后路径
     * @param oldName    原文件名
     * @param newName    修改后文件名
     * @return 文件的可访问路径
     */
    public String copy(String bucketName, String oldPath, String newPath, String oldName, String newName) {
        String oldObject = oldPath.concat(oldName);
        if (!ROOT_DIRECTORY.equals(oldPath)) {
            oldObject = oldPath.concat(ROOT_DIRECTORY).concat(oldName);
        }
        String newObject = newPath.concat(newName);
        if (!ROOT_DIRECTORY.equals(newPath)) {
            newObject = newPath.concat(ROOT_DIRECTORY).concat(newName);
        }
        try {
            CopySource copySource = CopySource.builder().bucket(bucketName).object(oldObject).build();
            minioClient.copyObject(CopyObjectArgs.builder().bucket(bucketName).object(newObject).source(copySource).build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
        return url + "/" + bucketName + newObject;
    }
}
