package com.xanqan.project.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisService redisService;

    @Test
    public void addList() {
//        String key = "path";
//        Map<Object, Object> map = new HashMap<>();
//        map.put("demo", "false");
//        map.put("demo1", "false");
//        redisService.setHash(key, map);
//        System.out.println(redisService.getHash(key));
//        System.out.println(redisService.getHash("sadfafsaf"));
    }
}
