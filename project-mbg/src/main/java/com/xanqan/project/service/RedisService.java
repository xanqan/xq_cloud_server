package com.xanqan.project.service;

import java.util.Map;

/**
 * redis操作Service
 *
 * @author xanqan
 */
public interface RedisService {
    /**
     * 存储数据
     */
    void set(String key, String value);

    void setHash(String key, Map<Object, Object> map);

    /**
     * 获取数据
     */
    String get(String key);

    Map<Object, Object> getHash(String key);

    /**
     * 设置超期时间
     */
    boolean expire(String key, long expire);

    /**
     * 删除数据
     */
    void remove(String key);

    /**
     * 自增操作
     *
     * @param delta 自增步长
     */
    Long increment(String key, long delta);
}
