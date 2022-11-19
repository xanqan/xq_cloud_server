package com.xanqan.project.service.impl;

import com.xanqan.project.service.RedisService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redis操作Service的实现类
 *
 * @author xanqan
 */
@Service
public class RedisServiceImpl implements RedisService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setHash(String key, Map<Object, Object> map) {
        stringRedisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public void setList(String key, List<String> shareUrls) {
        stringRedisTemplate.opsForList().leftPushAll(key, shareUrls);
    }

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Map<Object, Object> getHash(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    @Override
    public List<String> getList(String key) {
        return stringRedisTemplate.opsForList().range(key, 0, -1);
    }

    @Override
    public boolean expire(String key, Integer expire) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, expire, TimeUnit.DAYS));
    }

    @Override
    public void remove(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public void removeHashKey(String key, String hashKey) {
        stringRedisTemplate.opsForHash().delete(key, hashKey);
    }

    @Override
    public void removeList(String key, String value) {
        stringRedisTemplate.opsForList().remove(key, 1, value);
    }

    @Override
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }
}
