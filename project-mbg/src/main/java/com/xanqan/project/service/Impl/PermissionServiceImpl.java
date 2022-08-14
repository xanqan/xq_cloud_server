package com.xanqan.project.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xanqan.project.model.domain.Permission;
import com.xanqan.project.service.PermissionService;
import com.xanqan.project.mapper.PermissionMapper;
import org.springframework.stereotype.Service;

/**
* @author 28961
* @description 针对表【permission(权限)】的数据库操作Service实现
* @createDate 2022-08-13 16:18:58
*/
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission>
    implements PermissionService{

}




