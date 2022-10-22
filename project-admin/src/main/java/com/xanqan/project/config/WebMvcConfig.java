package com.xanqan.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局请求拦截器
 *
 * @author xanqan
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                //当**Credentials为true时，**Origin不能为星号，需为具体的ip地址
//                .allowedOrigins("*")
                //是否允许证书，不再默认开启
                .allowCredentials(true)
                //设置允许的方法和请求头
                .allowedHeaders("*")
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }
}
