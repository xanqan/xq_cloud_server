package com.xanqan.project.controller;

import cn.hutool.json.JSONUtil;
import com.xanqan.project.common.BaseResponse;
import com.xanqan.project.common.ResultUtils;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.model.dto.File;
import com.xanqan.project.model.vo.Home;
import com.xanqan.project.model.vo.UserInfo;
import com.xanqan.project.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 页面初始化请求
 *
 * @author xanqan
 */
@RestController
@RequestMapping("/pageInit")
@Api(tags = "页面初始化请求")
public class PageInitController {

    @Resource
    private FileService fileService;

    @Operation(summary = "主页数据初始化")
    @GetMapping("/home")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Home> getFileList(@RequestParam("user") String user) {
        List<File> result = fileService.getFileList("/", JSONUtil.toBean(user, User.class));
        UserInfo userInfo = new UserInfo(JSONUtil.toBean(user, User.class));
        Home home = new Home(userInfo, result);
        return ResultUtils.success(home);
    }

}
