package com.example.Yoga_fitness.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将uploads目录映射为静态资源
        String pathPattern = "/uploads/**";
        String resourceLocation = "file:" + uploadPath + "/";

        registry.addResourceHandler(pathPattern)
                .addResourceLocations(resourceLocation);

        System.out.println("已配置静态资源映射：" + pathPattern + " -> " + resourceLocation);
    }
}