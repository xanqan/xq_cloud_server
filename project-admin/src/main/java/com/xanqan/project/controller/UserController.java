package com.xanqan.project.controller;

import com.xanqan.project.common.BaseResponse;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.common.ResultUtils;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.service.UserAdminService;
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

    @Resource(name = "userAdminServiceImpl")
    private UserAdminService userAdminService;

    @Operation(summary = "查询用户",description = "根据 id 查询用户")
    @GetMapping("/search/{id}")
    public BaseResponse<User> getUser(@PathVariable("id") Integer id) {
        if (id <= 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "用户id小于等于0");
        }
        User user = userAdminService.getById(id);
        return ResultUtils.success(user);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public BaseResponse<Integer> register(@RequestBody User user) {
        int result = userAdminService.userRegister(user.getName(), user.getPassword());
        return ResultUtils.success("注册成功", result);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public BaseResponse<String> login(@RequestBody User user) {
        String token = userAdminService.userLogin(user.getName(), user.getPassword());
        return ResultUtils.success("登录成功", token);
    }

    @Operation(summary = "权限测试")
    @PostMapping("/testPermission")
    @PreAuthorize("hasAnyAuthority('read', 'write')")
    public BaseResponse<String> testPermission() {
        return ResultUtils.success("权限通过");
    }

}
