package com.fengrui.shortlink.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private Info createInfo(String title, String version){
        return new Info()
                .title(title)
                .version(version)
                .description("短链接开发环境文档");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createInfo("Short Link", "1.0"));
    }

}
