package com.xanqan.project.service;

import com.xanqan.project.model.domain.User;
import com.xanqan.project.model.dto.File;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

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