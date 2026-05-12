package com.coding.dto.deepseek;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek API 请求体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekRequest {
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 消息列表
     */
    private List<Message> messages;
    
    /**
     * 温度参数 (0-1)
     */
    private Double temperature;
    
    /**
     * 最大响应token数
     */
    private Integer max_tokens;
    
    /**
     * 消息对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 角色: system, user, assistant
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
    }
}
