package com.xanqan.project.service;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import com.xanqan.project.model.dto.File;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Resource;

import java.util.List;

@SpringBootTest
@Slf4j
class MongoDemoServiceTest {

    @Resource
    MongoTemplate mongoTemplate;

    @Test
    public void addTest(){
        File file = new File();
        file.setName("demo0");
        File result = mongoTemplate.insert(file, "1");
        log.info("result = " + result);
    }

    @Test
    public void findAllTest(){
        List<File> result = mongoTemplate.findAll(File.class,"1");
        log.info("result = " + result);
    }

    @Test
    public void findTest(){
        Query query = Query.query(Criteria.where("fileName").is("demo0"));
        List<File> result = mongoTemplate.find(query, File.class,"1");
        log.info("result = " + result);
    }

    @Test
    public void updateTest(){
        Query query_0 = Query.query(Criteria.where("fileName").is("demo0"));
        List<File> fileList = mongoTemplate.find(query_0, File.class,"1");
        File file = new File();
        file.setName("demo1");
        Query query_1 = Query.query(Criteria.where("_id").is(fileList.get(0).getId()));
        Update update = new Update();
        update.set("fileName",file.getName());
        UpdateResult result = mongoTemplate.upsert(query_1, update, File.class, "1");
        log.info("result = " + result);
    }

    @Test
    public void deleteTest(){
        Query query_0 = Query.query(Criteria.where("fileName").is("demo1"));
        List<File> fileList = mongoTemplate.find(query_0, File.class,"1");
        Query query_1 = Query.query(Criteria.where("_id").is(fileList.get(0).getId()));
        DeleteResult result = mongoTemplate.remove(query_1, "1");
        log.info("result = " + result);
    }
}