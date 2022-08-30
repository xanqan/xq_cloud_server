package com.xanqan.project.model.dto;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document
public class File {
    @MongoId
    private ObjectId id;
    @Field("fileName")
    private String fileName;
}
