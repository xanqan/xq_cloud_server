package com.xanqan.project.service;
import java.util.Date;
import org.bson.types.ObjectId;

import com.xanqan.project.model.dto.File;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
@Slf4j
public class FileServiceTest {

    @Resource
    private FileService fileService;

    @Test
    public void testPath() {
        String path = "/demo";
        String oldName = "demo";
        String oldFolderPath = "/demo/demo";
        String newFolderPath = "/demo/abc";
        File file = new File();
        file.setPath("/demo/demo/demo");
        String filePath = file.getPath();
        int i = filePath.indexOf(oldFolderPath) + oldFolderPath.length();
        file.setPath(newFolderPath + filePath.substring(i));
        System.out.println(file.getPath());
    }

}