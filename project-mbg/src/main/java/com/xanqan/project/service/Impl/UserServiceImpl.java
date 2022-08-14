package com.xanqan.project.service.Impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xanqan.project.model.domain.User;
import com.xanqan.project.service.UserService;
import com.xanqan.project.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 28961
* @description 针对表【user】的数据库操作Service实现
* @createDate 2022-08-13 14:19:26
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




