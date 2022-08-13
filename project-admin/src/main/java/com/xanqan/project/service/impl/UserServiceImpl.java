package com.xanqan.project.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.security.util.JwtTokenUtil;
import com.xanqan.project.service.UserService;
import com.xanqan.project.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author 28961
* @description 针对表【user】的数据库操作Service实现
* @createDate 2022-08-13 14:19:26
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserDetailsService userDetailsService;
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /** 用户账号最小位数 */
    public static final int MIN_USERNAME = 4;
    /** 用户密码最小位数 */
    public static final int MIN_PASSWORD = 6;
    /** 用户密码加密盐值 */
    public static final String PASSWORD_SECRET = "asdfgh";
    /** token负载开头 */
    private static final String TOKEN_HEAD = "Bearer";

    @Override
    public int userRegister(String userName, String password) {
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
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userName);
        if (matcher.find()) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "账号包含特殊字符");
        }

        // 账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "账号重复");
        }

        // 密码加密
        String encodePassword = passwordEncoder.encode(password);

        // 插入数据
        User user = new User();
        user.setUserName(userName);
        user.setPassword(encodePassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ResultCode.FAILED, "用户插入失败");
        }
        return user.getId();
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
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userName);
        if (matcher.find()) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "账号包含特殊字符");
        }

        // 对密码
        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "密码错误");
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        return jwtTokenUtil.generateToken(userDetails);
    }


}




