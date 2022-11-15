package com.xanqan.project.util;

import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.model.dto.File;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;

/**
 * 文件信息处理工具类
 *
 * @author xanqan
 */
@Component
public class FileUtil {

    private static final HashMap<String, String> VIEW_CONTENT_TYPE;
    private static final Set<String> PHOTO;
    private static final Set<String> VIDEO;
    private static final Set<String> AUDIO;

    static {
        VIEW_CONTENT_TYPE = new HashMap<>();
        VIEW_CONTENT_TYPE.put("default", "application/octet-stream");
        VIEW_CONTENT_TYPE.put("jpg", "image/jpeg");
        VIEW_CONTENT_TYPE.put("gif", "image/gif");
        VIEW_CONTENT_TYPE.put("png", "image/png");
        VIEW_CONTENT_TYPE.put("jpeg", "image/jpeg");
        VIEW_CONTENT_TYPE.put("mp4", "video/mp4");
        VIEW_CONTENT_TYPE.put("mp3", "audio/mpeg");
        PHOTO = new HashSet<>();
        String[] photoType = {"image/jpeg", "image/gif", "image/png",};
        PHOTO.addAll(Arrays.asList(photoType));
        VIDEO = new HashSet<>();
        String[] videoType = {"video/mp4"};
        VIDEO.addAll(Arrays.asList(videoType));
        AUDIO = new HashSet<>();
        String[] audioType = {"audio/mpeg"};
        AUDIO.addAll(Arrays.asList(audioType));
    }

    /**
     * 入口方法, 根据文件后缀再调用到对于的方法
     *
     * @param name 文件名
     * @return 文件类型
     */
    public String findType(String name) {
        String fileSuffix = name != null ? name.substring(name.lastIndexOf(".") + 1) : null;
        String contentType = VIEW_CONTENT_TYPE.get(fileSuffix);
        return contentType != null ? contentType : VIEW_CONTENT_TYPE.get("default");
    }


    public boolean isPhoto(String contentType) {
        return PHOTO.contains(contentType);
    }

    /**
     * 入口方法, 根据文件后缀再调用到对于的方法
     *
     * @param path          文件的路径
     * @param multipartFile 文件封装
     * @return 处理好的 File
     */
    public File read(String path, MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String name = fileName != null ? fileName.substring(fileName.lastIndexOf("/") + 1) : null;
        String contentType = multipartFile.getContentType();
        if (PHOTO.contains(contentType)) {
            return readPhoto(path, name, multipartFile);
        } else if (VIDEO.contains(contentType)) {
            return readVideo(path, name, multipartFile);
        } else if (AUDIO.contains(contentType)) {
            return readAudio(path, name, multipartFile);
        }
        return null;
    }

    private File readPhoto(String path, String name, MultipartFile multipartFile) {
        InputStream in = null;
        File file = new File();
        try {
            in = multipartFile.getInputStream();
            long fileSize = in.available();
            BufferedImage bufferedImage = ImageIO.read(in);
            String size = bufferedImage.getWidth() + "*" + bufferedImage.getHeight();
            file.setName(name);
            file.setPath(path);
            file.setFileSize(fileSize);
            file.setType(this.findType(name));
            file.setCreateTime(new Date());
            file.setModifyTime(new Date());
            file.setSize(size);
            file.setIsFolder(0);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private File readVideo(String path, String name, MultipartFile multipartFile) {
        InputStream in = null;
        File file = new File();
        try {
            in = multipartFile.getInputStream();
            long fileSize = in.available();
            file.setName(name);
            file.setPath(path);
            file.setFileSize(fileSize);
            file.setType(this.findType(name));
            file.setCreateTime(new Date());
            file.setModifyTime(new Date());
            file.setIsFolder(0);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }


    private File readAudio(String path, String name, MultipartFile multipartFile) {
        InputStream in = null;
        File file = new File();
        try {
            in = multipartFile.getInputStream();
            long fileSize = in.available();
            file.setName(name);
            file.setPath(path);
            file.setFileSize(fileSize);
            file.setType(this.findType(name));
            file.setCreateTime(new Date());
            file.setModifyTime(new Date());
            file.setIsFolder(0);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
