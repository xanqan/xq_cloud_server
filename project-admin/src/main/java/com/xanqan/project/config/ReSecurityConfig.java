package com.xanqan.project.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.mapper.UserPermissionMapper;
import com.xanqan.project.model.domain.Permission;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.model.dto.UserSecurity;
import com.xanqan.project.security.config.SecurityConfig;
import com.xanqan.project.service.UserPermissionService;
import com.xanqan.project.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import java.util.List;

/**
 * project-security模块相关配置
 *
 * @author xanqan
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ReSecurityConfig extends SecurityConfig {

    @Resource
    private UserService userService;
    @Resource
    private UserPermissionMapper userPermissionMapper;

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        //获取登录用户信息
        return username -> {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_name", username);
            User user = userService.getOne(queryWrapper);
            if (user != null) {
                List<Permission> permissionList = userPermissionMapper.getUserPermissionList(user.getId());
                return new UserSecurity(user,permissionList);
            }
            throw new BusinessException(ResultCode.PARAMS_ERROR, "用户名或密码错误");
        };
    }

}
