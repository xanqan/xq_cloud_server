package com.xanqan.project.model.dto;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Data
@Document
public class File {
    @MongoId
    private ObjectId id;

    @Field("name")
    private String name;

    @Field("path")
    private String path;

    @Field("fileSize")
    private long fileSize;

    @Field("createTime")
    private Date createTime;

    @Field("modifyTime")
    private Date modifyTime;

    @Field("size")
    private String size;

    @Field("duration")
    private String duration;
}
