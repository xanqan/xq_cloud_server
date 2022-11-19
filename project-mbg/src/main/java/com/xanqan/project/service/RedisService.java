package com.xanqan.project.service;

import java.util.List;
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

    /**
     * 存储hash
     */
    void setHash(String key, Map<Object, Object> map);

    /**
     * 储存list
     */
    void setList(String key, List<String> shareUrls);

    /**
     * 获取数据
     */
    String get(String key);

    /**
     * 获取hash
     */
    Map<Object, Object> getHash(String key);

    /**
     * 获取List
     */
    List<String> getList(String key);

    /**
     * 设置超期时间
     */
    boolean expire(String key, Integer expire);

    /**
     * 删除数据
     */
    void remove(String key);

    /**
     * 删除map中的key
     */
    void removeHashKey(String key, String hashKey);

    /**
     * 删除list中的值
     */
    void removeList(String key, String value);

    /**
     * 自增操作
     *
     * @param delta 自增步长
     */
    Long increment(String key, long delta);
}
