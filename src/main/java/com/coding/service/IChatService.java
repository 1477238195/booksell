package com.coding.service;

import com.coding.entity.ChatSession;
import com.coding.entity.ChatMessage;
import java.util.List;

public interface IChatService {
    // 会话管理
    List<ChatSession> getUserSessions(Long userId);
    ChatSession getSessionWithMessages(Long sessionId, Long userId);
    ChatSession createSession(Long userId, String title);
    void deleteSession(Long sessionId, Long userId);
    
    // 消息管理
    ChatMessage addMessage(Long sessionId, Long userId, Boolean isUser, String content);
    void deleteMessage(Long messageId, Long userId);
}
