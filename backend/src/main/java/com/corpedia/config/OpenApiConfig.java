package com.corpedia.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Knife4j / springdoc-openapi 文档配置。
 * 全局 Bearer JWT 认证：除登录外的接口需带 Authorization: Bearer <token>（@SecurityRequirements 已在 login 上解除）。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI corpediaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Corpedia 企业内部知识库客服系统 API")
                        .description("""
                                基于 RAG 的企业内部知识库客服系统后端接口。
                                统一返回结构：`{code, msg, data}`（成功 code=0）；接口前缀 /api；除 /auth/login 外均需 JWT 认证。
                                """)
                        .version("v1.0"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .tags(List.of(
                        new Tag().name("认证").description("登录 / 登出 / 当前用户"),
                        new Tag().name("知识库管理").description("知识库 CRUD（删除级联清理文档与向量）"),
                        new Tag().name("文档管理").description("文档上传 / 列表 / 详情 / 删除 / 状态查询（含入库轮询端点）"),
                        new Tag().name("文档入库管道").description("触发 / 重试文档解析入库（异步处理）"),
                        new Tag().name("AI 冒烟探针").description("硬骨头1 验证：LM Studio chat / bge-m3 embedding(1024维) / Milvus 连通")
                ));
    }
}
