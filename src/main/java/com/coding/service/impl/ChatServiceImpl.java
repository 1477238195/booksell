package com.coding.service.impl;

import com.coding.entity.ChatMessage;
import com.coding.entity.ChatSession;
import com.coding.mapper.ChatMessageMapper;
import com.coding.mapper.ChatSessionMapper;
import com.coding.service.IChatService;
import com.coding.utils.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        return sessionMapper.selectByUserId(userId);
    }

    @Override
    public ChatSession getSessionWithMessages(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectByPrimaryKey(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return null;
        }
        List<ChatMessage> messages = messageMapper.selectBySessionId(sessionId);
        session.setMessages(messages);
        return session;
    }

    @Override
    @Transactional
    public ChatSession createSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新对话");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.insertSelective(session);
        return session;
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectByPrimaryKey(sessionId);
        if (session != null && session.getUserId().equals(userId)) {
            messageMapper.deleteBySessionId(sessionId);
            sessionMapper.deleteByPrimaryKey(sessionId);
        }
    }

    @Override
    @Transactional
    public ChatMessage addMessage(Long sessionId, Long userId, Boolean isUser, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setIsUser(isUser);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insertSelective(message);
        
        ChatSession session = sessionMapper.selectByPrimaryKey(sessionId);
        if (session != null) {
            session.setUpdatedAt(LocalDateTime.now());
            sessionMapper.updateByPrimaryKeySelective(session);
        }
        
        return message;
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        ChatMessage message = messageMapper.selectByPrimaryKey(messageId);
        if (message != null && message.getUserId().equals(userId)) {
            messageMapper.deleteByPrimaryKey(messageId);
        }
    }
}
