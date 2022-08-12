package com.xanqan.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

    /**
     * 用于读取配置文件 application.properties 中 swagger 属性是否开启
     */
    @Value("${springfox.documentation.swagger-ui.enabled}")
    Boolean swaggerEnabled;

    @Bean
    Docket docket() {
        // 设置 swagger的版本
        return new Docket(DocumentationType.OAS_30)
                // 是否开启swagger
                .enable(swaggerEnabled)
                // 选择生成接口文档
                .select()
                // 包所在的路径
                .apis(RequestHandlerSelectors.basePackage("com.xanqan.project.controller"))
                // 当前包下所有接口都生成
                .paths(PathSelectors.any())
                .build()
                // 接口文档初始化，也就是设置接口文档的详细信息，
                .apiInfo(
                        new ApiInfoBuilder()
                                .description("xxx 项目接口文档")
                                // 联系人
                                .contact(new Contact("xanqan","https://xanqan.icu","2896106853@qq.com"))
                                .version("v1.0")
                                .title("API 测试文档")
                                .license("Apache 2.0")
                                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
                                .build()
                );
    }
}
