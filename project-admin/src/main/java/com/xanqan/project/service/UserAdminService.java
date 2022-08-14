package com.xanqan.project.service;

/**
 * user服务接口类,mbg模块的重写
 *
 * @author xanqan
 */
public interface UserAdminService extends UserService{
    /**
     * 用户注册
     *
     * @param userName 用户账户
     * @param password 用户密码
     * @return 新用户 id
     */
    int userRegister(String userName, String password);


    /**
     * 用户注册
     *
     * @param userName 用户账户
     * @param password 用户密码
     * @return 新用户 id
     */
    String userLogin(String userName, String password);
}
