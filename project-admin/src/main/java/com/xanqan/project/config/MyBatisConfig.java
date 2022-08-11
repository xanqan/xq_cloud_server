package com.xanqan.project.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis相关配置
 *
 * @author xanqan
 */
@Configuration
@MapperScan({"com.xanqan.project.mapper"})
public class MyBatisConfig {
}
