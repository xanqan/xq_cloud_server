package com.xanqan.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xanqan.project.model.domain.Permission;
import com.xanqan.project.model.domain.User;

import java.util.List;

/**
 * user服务接口,mbg模块的重写
 *
 * @author xanqan
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户id获取全部权限
     *
     * @param id 用户id
     * @return 权限列表
     */
    List<Permission> getUserPermissionsById(int id);

    /**
     * 用户注册
     *
     * @param userName 用户账户
     * @param password 用户密码
     * @return 新用户 id
     */
    boolean userRegister(String userName, String password);

    /**
     * 用户登录
     *
     * @param userName 用户账户
     * @param password 用户密码
     * @return token
     */
    String userLogin(String userName, String password);

    /**
     * 用户申请增加容量
     *
     * @param modifySize 修改的值
     * @param user       用户信息
     * @return 申请成功
     */
    boolean applyModifyCapacity(Long modifySize, User user);
}
