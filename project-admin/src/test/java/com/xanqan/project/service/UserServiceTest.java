package com.xanqan.project.service;

import com.xanqan.project.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

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

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUserName("demo3");
        user.setPassword("123456");
        int result = userAdminService.userRegister(user.getUserName(), user.getPassword());
        log.info("result = " + result);
    }
}
