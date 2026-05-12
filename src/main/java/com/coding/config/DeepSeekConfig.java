package com.coding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek API 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * API 基础URL
     */
    private String baseUrl = "https://api.deepseek.com/v1";
    
    /**
     * 默认模型名称
     */
    private String model = "deepseek-v4-flash";
    
    /**
     * 默认温度参数
     */
    private Double temperature = 0.7;
    
    /**
     * 最大响应token数
     */
    private Integer maxTokens = 2048;
}
