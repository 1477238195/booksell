package com.coding.dto.deepseek;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek API 响应体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekResponse {
    
    /**
     * 响应ID
     */
    private String id;
    
    /**
     * 对象类型
     */
    private String object;
    
    /**
     * 创建时间戳
     */
    private Long created;
    
    /**
     * 使用的模型
     */
    private String model;
    
    /**
     * 响应内容列表
     */
    private List<Choice> choices;
    
    /**
     * 令牌使用统计
     */
    private Usage usage;
    
    /**
     * 选项对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        /**
         * 索引
         */
        private Integer index;
        
        /**
         * 消息
         */
        private DeepSeekRequest.Message message;
        
        /**
         * 结束原因
         */
        private String finish_reason;
    }
    
    /**
     * 使用统计对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * 提示词token数
         */
        private Integer prompt_tokens;
        
        /**
         * 响应token数
         */
        private Integer completion_tokens;
        
        /**
         * 总token数
         */
        private Integer total_tokens;
    }
}
