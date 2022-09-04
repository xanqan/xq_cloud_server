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

@Component
public class FileUtil {

    public static Set<String> photo;

    static {
        photo = new HashSet<>();
        String[] photoType = {".png", ".jpg", ".jpeg", ".gif", ".bmp"};
        photo.addAll(Arrays.asList(photoType));
    }

    public File read(String path, MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String fileSuffix = fileName != null ? fileName.substring(fileName.lastIndexOf(".")) : null;
        if (photo.contains(fileSuffix)) {
            return readImg(path, multipartFile);
        }
        return null;
    }

    public File readImg(String path, MultipartFile multipartFile) {
        InputStream in = null;
        File file = new File();
        try {
            in = multipartFile.getInputStream();
            long fileSize = in.available();
            BufferedImage bufferedImage = ImageIO.read(in);
            String size = bufferedImage.getWidth() + "*" + bufferedImage.getHeight();
            file.setName(multipartFile.getOriginalFilename());
            file.setPath(path);
            file.setFileSize(fileSize);
            file.setCreateTime(new Date());
            file.setModifyTime(new Date());
            file.setSize(size);
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
