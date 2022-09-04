package com.xanqan.project.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIo 配置类
 *
 * @author xanqan
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinIoClientConfig {

    /**
     * minio 服务器地址
     */
    private String url;

    /**
     * minio accessKey
     */
    private String accessKey;

    /**
     * minio secretKey
     */
    private String secretKey;

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }

}
