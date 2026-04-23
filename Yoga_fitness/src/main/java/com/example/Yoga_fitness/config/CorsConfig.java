package com.example.Yoga_fitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration // 标记为配置类
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 配置跨域规则
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 允许携带Cookie
        config.addAllowedOriginPattern("http://127.0.0.1:5500/"); // 允许前端域名（按需修改）
        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许所有请求方法（GET/POST/PUT等）

        // 2. 应用跨域规则到所有接口
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 匹配所有接口路径

        return new CorsFilter(source);
    }
}