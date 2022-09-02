package com.xanqan.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xanqan.project.model.domain.Permission;
import com.xanqan.project.model.dto.UserPermissionName;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 针对 UserPermissionName 操作
 *
 * @author xanqan
 */
public interface UserPermissionNameMapper extends BaseMapper<UserPermissionName> {

    @Select("select p.name from user_permission up join permission p on p.id = up.permission_id where user_id = #{userId}")
    List<Permission> getUserPermissionNameList(@Param("userId") int userId);

}
