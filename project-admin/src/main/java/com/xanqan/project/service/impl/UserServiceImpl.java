package com.xanqan.project.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.mapper.PermissionMapper;
import com.xanqan.project.mapper.UserMapper;
import com.xanqan.project.model.domain.Permission;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.model.domain.UserPermission;
import com.xanqan.project.security.util.JwtTokenUtil;
import com.xanqan.project.service.UserPermissionService;
import com.xanqan.project.service.UserService;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Pattern;

/**
 * user服务类,mbg模块的重写
 *
 * @author xanqan
 */
@Service
@Primary
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private PermissionMapper permissionMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserDetailsService userDetailsService;
    @Resource
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private UserPermissionService userPermissionService;

    /**
     * 用户账号最小位数
     */
    public static final int MIN_USERNAME = 4;
    /**
     * 用户密码最小位数
     */
    public static final int MIN_PASSWORD = 6;
    /**
     * 用户密码最小位数
     */
    public static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    public static boolean pattern(String str) {
        return !PATTERN.matcher(str).matches();
    }

    @Override
    public List<Permission> getUserPermissionsById(int id) {
        return permissionMapper.getUserPermissionsById(id);
    }

    @Override
    public boolean userRegister(String userName, String password) {
        // 校验
        if (StrUtil.hasBlank(userName, password)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() < MIN_USERNAME) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "用户账号小于4位");
        }
        if (password.length() < MIN_PASSWORD) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "用户密码小于6位");
        }

        // 账号不能包含特殊字符
        if (pattern(userName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "账号包含特殊字符");
        }

        // 账号不能重复
        long count = this.count(new QueryWrapper<User>()
                .eq("name", userName));
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "账号重复");
        }

        // 密码加密
        String encodePassword = passwordEncoder.encode(password);

        // 插入数据
        User user = new User();
        user.setName(userName);
        user.setPassword(encodePassword);
        user.setSizeMax(5242880L);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ResultCode.FAILED, "用户插入失败");
        }
        UserPermission userPermission = new UserPermission();
        userPermission.setUserId((Integer) user.getId());
        userPermission.setPermissionId(1);
        userPermissionService.save(userPermission);
        return true;
    }

    @Override
    public String userLogin(String userName, String password) {
        // 校验
        if (StrUtil.hasBlank(userName, password)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() < MIN_USERNAME) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "用户账号小于4位");
        }
        if (password.length() < MIN_PASSWORD) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "用户密码小于6位");
        }

        // 账号不能包含特殊字符
        if (pattern(userName)) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "账号包含特殊字符");
        }

        // 对密码
        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        if (!userDetails.isEnabled()) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "用户已禁用");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "密码错误");
        }
        // 更新security登录用户对象
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        // 将authenticationToken放入spring security全局中
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        return jwtTokenUtil.generateToken(userDetails);
    }

    @Override
    public boolean applyModifyCapacity(Long modifySize, User user) {
        if (modifySize < 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "不能申请负数");
        }
        if (modifySize < user.getSizeUse() && modifySize != 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "不能低于已使用容量");
        }

        user.setModifySize(modifySize);
        this.updateById(user);
        return true;
    }

}
