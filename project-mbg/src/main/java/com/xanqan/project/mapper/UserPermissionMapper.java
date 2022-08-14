package com.xanqan.project.mapper;

import com.xanqan.project.model.domain.Permission;
import com.xanqan.project.model.domain.UserPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 28961
* @description 针对表【user_permission】的数据库操作Mapper
* @createDate 2022-08-13 16:19:20
* @Entity com.xanqan.project.model.domain.UserPermission
*/
public interface UserPermissionMapper extends BaseMapper<UserPermission> {

    /**
     * 根据用户id查询其全部权限名称
     *
     * @param userId 用户id
     * @return 权限名称
     */
    @Select("select permission_name from user_permission join permission p on p.id = user_permission.permission_id where user_id = ${userId}")
    List<Permission> getUserPermissionList(@Param("userId") int userId);
}




