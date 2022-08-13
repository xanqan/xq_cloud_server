package com.xanqan.project.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName user_permission
 */
@TableName(value ="user_permission")
@Data
public class UserPermission implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private Integer userId;

    /**
     * 
     */
    private Integer permissionId;

    /**
     *
     */
    private String permissionName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}