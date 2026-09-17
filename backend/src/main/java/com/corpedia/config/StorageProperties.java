package com.corpedia.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 上传文件落盘目录（application.yml corpedia.storage.*）。
 */
@Data
@ConfigurationProperties(prefix = "corpedia.storage")
public class StorageProperties {

    private String path = "./storage/documents";
}
