package com.xanqan.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xanqan.project.model.domain.Permission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * 针对 UserPermissionName 操作, 为 ReSecurityConfig 服务
 *
 * @author xanqan
 */
@Primary
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户id获取其全部权限
     *
     * @param userId 用户id
     * @return 权限列表
     */
    @Select("select p.name from user_permission up join permission p on p.id = up.permission_id where user_id = #{userId}")
    List<Permission> getUserPermissionsById(@Param("userId") int userId);

}
