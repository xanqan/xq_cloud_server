package com.xanqan.project.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 在 UserPermission 的基础上增加了 PermissionName 字段
 *
 * @author xanqan
 */
@Data
public class UserPermissionName implements Serializable {
    /**
     * 自增id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 权限id
     */
    private Integer permissionId;

    /**
     * 权限名字
     */
    private Integer permissionName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
