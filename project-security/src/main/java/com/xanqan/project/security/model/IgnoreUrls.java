package com.xanqan.project.security.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于配置不需要保护的资源路径
 *
 * @author xanqan
 */
@Data
@Component
@ConfigurationProperties(prefix = "secure.ignored")
public class IgnoreUrls {

    private List<String> urls = new ArrayList<>();

}
