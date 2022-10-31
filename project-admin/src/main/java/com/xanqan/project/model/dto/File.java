package com.xanqan.project.model.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

/**
 * mongodb 文件信息封装类
 *
 * @author xanqan
 */
@Data
@Document
public class File {

    /**
     * 主键,由 mongodb 自动生成，对应 _id 字段
     */
    @MongoId
    private String id;

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
     * 文件大小
     */
    @Field("fileSize")
    private long fileSize;

    /**
     * 文件类型
     */
    @Field("type")
    private String type;

    /**
     * 创建时间
     */
    @Field("createTime")
    private Date createTime;

    /**
     * 修改时间
     */
    @Field("modifyTime")
    private Date modifyTime;

    /**
     * 文件宽高,仅有一部分文件格式需要，下同
     */
    @Field("size")
    private String size;

    /**
     * 文件时长
     */
    @Field("duration")
    private String duration;

    /**
     * 是否为文件夹
     */
    @Field("isFolder")
    private int isFolder;
}
