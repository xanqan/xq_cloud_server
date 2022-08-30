package com.xanqan.project.service.impl;

import com.xanqan.project.service.MongoDemoService;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.Resource;

public class MongoDemoServiceImpl implements MongoDemoService {

    @Resource
    MongoTemplate mongoTemplate;


}
