package com.xanqan.project.service.impl;

import com.xanqan.project.service.RedisService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Map<Object, Object> getHash(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    @Override
    public boolean expire(String key, long expire) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, expire, TimeUnit.SECONDS));
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
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }
}
