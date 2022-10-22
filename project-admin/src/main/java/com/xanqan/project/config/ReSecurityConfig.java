package com.xanqan.project.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.model.domain.Permission;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.security.config.SecurityConfig;
import com.xanqan.project.security.model.UserSecurity;
import com.xanqan.project.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

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

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        //获取登录用户信息
        return userName -> {
            User user = userService.getOne(new QueryWrapper<User>()
                    .eq("name", userName));
            if (user != null) {
                List<Permission> permissionList = userService.getUserPermissionsById(user.getId());
                return new UserSecurity(user, permissionList);
            }
            throw new BusinessException(ResultCode.PARAMS_ERROR, "用户名或密码错误");
        };
    }

}
