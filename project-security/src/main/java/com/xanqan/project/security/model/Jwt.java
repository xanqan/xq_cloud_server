package com.xanqan.project.security.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用于配置jwt
 *
 * @author xanqan
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class Jwt {
    private String tokenHead;

    private String tokenHeader;

    private String secret;

    private int expireTime;

    private int justTime;
}
