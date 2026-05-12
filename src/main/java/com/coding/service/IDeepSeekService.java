package com.coding.service;

import com.coding.utils.R;

/**
 * DeepSeek AI 服务接口
 */
public interface IDeepSeekService {
    
    /**
     * 对话聊天
     * @param userMessage 用户消息
     * @return 响应结果
     */
    R<String> chat(String userMessage);
    
    /**
     * 书籍推荐
     * @param userPreferences 用户偏好描述
     * @return 推荐结果
     */
    R<String> recommendBooks(String userPreferences);
    
    /**
     * 书籍讨论
     * @param bookName 书籍名称
     * @param discussionTopic 讨论主题
     * @return 讨论结果
     */
    R<String> discussBook(String bookName, String discussionTopic);
}
