package com.xanqan.project.model.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

/**
 * mongodb 文件分享封装类
 *
 * @author xanqan
 */
@Data
@Document
public class Share {

    /**
     * 主键,由 mongodb 自动生成，对应 _id 字段
     */
    @MongoId
    private String id;

    /**
     * 文件分享url
     */
    @Field("url")
    private String url;

    /**
     * 文件分享url
     */
    @Field("password")
    private String password;

    /**
     * 道歉时间
     */
    @Field("expire")
    private Date expire;

    /**
     * 文件名
     */
    @Field("name")
    private String name;

    /**
     * 文件路径
     */
    @Field("path")
    private String path;

    /**
     * 文件类型
     */
    @Field("type")
    private String type;
}
