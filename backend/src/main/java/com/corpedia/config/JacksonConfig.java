package com.corpedia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 显式提供 ObjectMapper 单例 Bean：供服务层注入（会话来源 JSON 序列化/反序列化）。
 * 注册 JavaTimeModule 并关闭时间戳，LocalDateTime 以 ISO 字符串序列化（与阶段2 MVC 行为一致）。
 */
@Configuration
public class JacksonConfig {

    /**
     * 覆盖自动配置缺失的 ObjectMapper Bean。
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}