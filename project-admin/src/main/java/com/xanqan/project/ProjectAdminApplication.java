package com.xanqan.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * admin模块启动类
 *
 * @author xanqan
 */
@SpringBootApplication
public class ProjectAdminApplication {
    //使druid链接超时error不再写入日志
    static {
        System.setProperty("druid.mysql.usePingMethod","false");
    }

    public static void main(String[] args) {
        SpringApplication.run(ProjectAdminApplication.class, args);
    }

}
