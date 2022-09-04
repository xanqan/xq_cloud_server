package com.xanqan.project.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

    @Override
    public String upload(String path, MultipartFile multipartFile, HttpServletRequest request) {
        // 校验
        if (StrUtil.hasBlank(path)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (multipartFile.isEmpty() || multipartFile.getSize() <= 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "文件为空");
        }

        //用户id获取
        String authHeader = request.getHeader(tokenHeader);
        String authToken = authHeader.substring(tokenHead.length());
        String userName = jwtTokenUtil.getUserNameFromToken(authToken);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", userName);
        User user = userAdminService.getOne(queryWrapper);

        String bucketName = "xq" + user.getId().toString();
        String result = minioUtil.upload(bucketName, path, multipartFile);
        File file = fileUtil.read(path, multipartFile);
        mongoTemplate.insert(file, bucketName);
        return result;
    }
}
