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

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUserName("demo");
        boolean result = userService.save(user);
        log.info("result = " + result);
    }
}
