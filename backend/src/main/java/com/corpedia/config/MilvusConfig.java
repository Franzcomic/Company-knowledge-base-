package com.corpedia.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 原生 Milvus v2 客户端：用于集合 schema/索引初始化与按表达式删除（Spring AI VectorStore 无 delete-by-filter）。
 */
@Configuration
public class MilvusConfig {

    @Bean
    public MilvusClientV2 milvusClientV2(Environment env) {
        String host = env.getProperty("spring.ai.vectorstore.milvus.client.host", "localhost");
        int port = env.getProperty("spring.ai.vectorstore.milvus.client.port", Integer.class, 19530);
        return new MilvusClientV2(ConnectConfig.builder()
                .uri("http://" + host + ":" + port)
                .build());
    }
}
