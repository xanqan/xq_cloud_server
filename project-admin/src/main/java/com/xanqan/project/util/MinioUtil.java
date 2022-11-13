package com.xanqan.project.util;

import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.model.dto.File;
import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MinIO 服务封装工具类
 *
 * @author xanqan
 */
@Component
public class MinioUtil {
    @Resource
    private MinioClient minioClient;
    @Resource
    private FileUtil fileUtil;

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

    public GetObjectResponse getFile(String bucketName, String path, String filename) {
        String object;
        if (ROOT_DIRECTORY.equals(path)) {
            object = ROOT_DIRECTORY + filename;
        } else {
            object = path + ROOT_DIRECTORY + filename;
        }
        GetObjectResponse getObjectResponse = null;
        try {
            getObjectResponse = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(object).build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getObjectResponse;
    }

    /**
     * 文件上传
     *
     * @param bucketName    存储桶名
     * @param file          文件信息
     * @param multipartFile 文件封装
     * @return 文件的可访问路径
     */
    public String upload(String bucketName, File file, MultipartFile multipartFile) {
        existBucket(bucketName);
        String object;
        if (ROOT_DIRECTORY.equals(file.getPath())) {
            object = ROOT_DIRECTORY + file.getName();
        } else {
            object = file.getPath() + ROOT_DIRECTORY + file.getName();
        }
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
        return url + ROOT_DIRECTORY + bucketName + object;
    }

    public void uploadImg(String bucketName, String fileName, InputStream in) {
        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(ROOT_DIRECTORY.concat(fileName).concat(".jpg"))
                    .stream(in, in.available(), -1)
                    .contentType("image/jpeg")
                    .build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    public void compose(String bucketName, String path, String fileName) {
        String object = path.concat(fileName);
        String objectChunk = object.concat("_chunk");
        List<String> objects = new ArrayList<>();
        List<ComposeSource> sources = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(objectChunk).recursive(true).build());
            for (Result<Item> itemResult : results) {
                objects.add(itemResult.get().objectName());
            }
            for (String sourcesObject : objects) {
                sources.add(ComposeSource.builder().bucket(bucketName).object(sourcesObject).build());
            }
            Map<String, String> userMetadata = new HashMap<>(2);
            userMetadata.put("Content-Type", fileUtil.findType(object));
            minioClient.composeObject(ComposeObjectArgs.builder().bucket(bucketName).object(object).sources(sources).extraHeaders(userMetadata).build());
            for (String sourcesObject : objects) {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(sourcesObject).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        String object = path.concat(ROOT_DIRECTORY).concat(fileName);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(object).build());
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
        String oldObject = oldPath.concat(ROOT_DIRECTORY).concat(oldName);
        String newObject = newPath.concat(ROOT_DIRECTORY).concat(newName);
        try {
            CopySource copySource = CopySource.builder().bucket(bucketName).object(oldObject).build();
            minioClient.copyObject(CopyObjectArgs.builder().bucket(bucketName).object(newObject).source(copySource).build());
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(oldObject).build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
        return url + ROOT_DIRECTORY + bucketName + newObject;
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
        String oldObject = oldPath.concat(ROOT_DIRECTORY).concat(oldName);
        String newObject = newPath.concat(ROOT_DIRECTORY).concat(newName);
        try {
            CopySource copySource = CopySource.builder().bucket(bucketName).object(oldObject).build();
            minioClient.copyObject(CopyObjectArgs.builder().bucket(bucketName).object(newObject).source(copySource).build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
        return url + ROOT_DIRECTORY + bucketName + newObject;
    }
}
