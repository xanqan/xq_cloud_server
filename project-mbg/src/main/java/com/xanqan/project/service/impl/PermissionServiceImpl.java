package com.xanqan.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xanqan.project.model.domain.Permission;
import com.xanqan.project.service.PermissionService;
import com.xanqan.project.mapper.PermissionMapper;
import org.springframework.stereotype.Service;

/**
* @author 28961
* @description 针对表【permission(权限表)】的数据库操作Service实现
* @createDate 2022-09-02 13:57:01
*/
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission>
    implements PermissionService{

}




