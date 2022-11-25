package com.xanqan.project.controller;

import cn.hutool.json.JSONUtil;
import com.xanqan.project.common.BaseResponse;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.common.ResultUtils;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.model.vo.LoginVo;
import com.xanqan.project.model.vo.UserInfo;
import com.xanqan.project.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * User请求处理
 *
 * @author xanqan
 */
@RestController
@RequestMapping("/user")
@Api(tags = "User请求处理")
public class UserController {
    @Resource
    private UserService userService;

    @Operation(summary = "查询用户", description = "根据 id 查询用户")
    @GetMapping("/search/{id}")
    public BaseResponse<User> getUser(@PathVariable("id") Integer id) {
        if (id <= 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "用户id小于等于0");
        }
        User user = userService.getById(id);
        return ResultUtils.success(user);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public BaseResponse<Boolean> register(@RequestBody User user) {
        boolean result = userService.userRegister(user.getName(), user.getPassword());
        return ResultUtils.success("注册成功", result);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public BaseResponse<LoginVo> login(@RequestBody User user) {
        LoginVo loginVo = userService.userLogin(user.getName(), user.getPassword());
        return ResultUtils.success("登录成功", loginVo);
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/getUserInfo")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<UserInfo> getUserInfo(@RequestParam("user") String user) {
        UserInfo userInfo = new UserInfo(JSONUtil.toBean(user, User.class));
        return ResultUtils.success(userInfo);
    }

    @Operation(summary = "用户申请增加容量")
    @GetMapping("/applyModifyCapacity")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<Boolean> applyModifyCapacity(@RequestParam("modifySize") Long modifySize,
                                                     @RequestParam("user") String user) {
        boolean result = userService.applyModifyCapacity(modifySize, JSONUtil.toBean(user, User.class));
        return ResultUtils.success(result);
    }

    @Operation(summary = "权限测试")
    @PostMapping("/testPermission")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<String> testPermission() {
        return ResultUtils.success("权限通过");
    }

}
