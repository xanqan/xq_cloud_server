package com.xanqan.project.util;

import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.model.dto.File;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件信息处理工具类
 *
 * @author xanqan
 */
@Component
public class FileUtil {

    public static Set<String> photo;
    public static final String PHOTO = "photo";

    static {
        photo = new HashSet<>();
        String[] photoType = {".png", ".jpg", ".jpeg", ".gif", ".bmp"};
        photo.addAll(Arrays.asList(photoType));
    }

    /**
     * 入口方法, 根据文件后缀再调用到对于的方法
     *
     * @param name 文件名
     * @return 文件类型
     */
    public String findType(String name) {
        String fileSuffix = name != null ? name.substring(name.lastIndexOf(".")) : null;
        if (photo.contains(fileSuffix)) {
            return PHOTO;
        }
        return null;
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
        String fileSuffix = fileName != null ? fileName.substring(fileName.lastIndexOf(".")) : null;
        String name = fileName != null ? fileName.substring(fileName.lastIndexOf("/") + 1) : null;
        if (photo.contains(fileSuffix)) {
            return readPhoto(path, name, multipartFile);
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
            file.setType(PHOTO);
            file.setCreateTime(new Date());
            file.setModifyTime(new Date());
            file.setSize(size);
            file.setIsFolder(0);
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
        return file;
    }

}
