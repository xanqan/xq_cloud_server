package com.xanqan.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xanqan.project.model.domain.UserPermission;
import com.xanqan.project.service.UserPermissionService;
import com.xanqan.project.mapper.UserPermissionMapper;
import org.springframework.stereotype.Service;

/**
* @author 28961
* @description 针对表【user_permission】的数据库操作Service实现
* @createDate 2022-08-13 16:19:20
*/
@Service
public class UserPermissionServiceImpl extends ServiceImpl<UserPermissionMapper, UserPermission>
    implements UserPermissionService{

}




