package com.coding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI聊天请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {
    
    /**
     * 用户消息内容
     */
    private String message;
}
