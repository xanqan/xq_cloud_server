package com.xanqan.project.service;

import com.xanqan.project.mapper.UserPermissionNameMapper;
import com.xanqan.project.model.domain.Permission;
import com.xanqan.project.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户服务测试
 *
 * @author xanqan
 */
@SpringBootTest
@Slf4j
public class UserServiceTest {

    @Resource(name = "userAdminServiceImpl")
    private UserAdminService userAdminService;

    @Resource
    private UserPermissionNameMapper userPermissionNameMapper;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setName("demo3");
        user.setPassword("123456");
        int result = userAdminService.userRegister(user.getName(), user.getPassword());
        log.info("result = " + result);
    }

    @Test
    public void test() {
        List<Permission> permissionList = userPermissionNameMapper.getUserPermissionNameList(1);
        log.info("result = " + permissionList.toString());
    }
}
