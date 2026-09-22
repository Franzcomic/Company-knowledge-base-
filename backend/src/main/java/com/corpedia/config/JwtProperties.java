package com.corpedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置（application.yml corpedia.jwt.*）：签名密钥与过期秒数。
 */
@Configuration
@ConfigurationProperties(prefix = "corpedia.jwt")
public class JwtProperties {
    /** 签名密钥（过短时由 JwtUtil 用 SHA-256 派生）。 */
    private String secret;
    /** 过期秒数，默认 86400（1 天）。 */
    private long expireSeconds = 86400;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getExpireSeconds() { return expireSeconds; }
    public void setExpireSeconds(long expireSeconds) { this.expireSeconds = expireSeconds; }
}