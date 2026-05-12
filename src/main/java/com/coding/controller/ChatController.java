package com.coding.controller;

import com.coding.entity.ChatMessage;
import com.coding.entity.ChatSession;
import com.coding.service.IChatService;
import com.coding.service.IDeepSeekService;
import com.coding.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Api(tags = "AI 聊天服务")
@RequestMapping("/api/ai")
@RestController
public class ChatController {

    private final IChatService chatService;
    private final IDeepSeekService deepSeekService;

    @ApiOperation("获取用户所有会话")
    @GetMapping("/sessions")
    public R<List<ChatSession>> getSessions(HttpServletRequest request) {
        Long userId = getUserId(request);
        return R.createBySuccess(chatService.getUserSessions(userId));
    }

    @ApiOperation("获取会话详情(含消息)")
    @GetMapping("/sessions/{id}")
    public R<ChatSession> getSession(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        ChatSession session = chatService.getSessionWithMessages(id, userId);
        if (session == null) {
            return R.createByErrorMessage("会话不存在");
        }
        return R.createBySuccess(session);
    }

    @ApiOperation("创建新会话")
    @PostMapping("/sessions")
    public R<ChatSession> createSession(@RequestBody String title, HttpServletRequest request) {
        Long userId = getUserId(request);
        return R.createBySuccess(chatService.createSession(userId, title));
    }

    @ApiOperation("删除会话")
    @DeleteMapping("/sessions/{id}")
    public R<Void> deleteSession(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        chatService.deleteSession(id, userId);
        return R.createBySuccess();
    }

    @ApiOperation("发送消息并获取AI回复")
    @PostMapping("/chat")
    public R<ChatMessage> sendMessage(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long sessionId = chatRequest.getSessionId();
        String userContent = chatRequest.getContent();
        
        // 保存用户消息
        ChatMessage userMsg = chatService.addMessage(sessionId, userId, true, userContent);
        
        // 调用 AI
        R<String> aiResponse = deepSeekService.chat(userContent);
        String aiContent = aiResponse.getData();
        
        // 保存 AI 回复
        ChatMessage aiMsg = chatService.addMessage(sessionId, userId, false, aiContent);
        
        return R.createBySuccess(aiMsg);
    }

    @ApiOperation("仅保存消息(不调用AI)")
    @PostMapping("/message")
    public R<ChatMessage> saveMessage(@RequestBody SaveMessageRequest req, HttpServletRequest request) {
        Long userId = getUserId(request);
        return R.createBySuccess(chatService.addMessage(req.getSessionId(), userId, req.getIsUser(), req.getContent()));
    }

    private Long getUserId(HttpServletRequest request) {
        // 从 session 或 token 获取 userId，暂用固定值
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException ignored) {}
        }
        return 1L;
    }

    public static class ChatRequest {
        private Long sessionId;
        private String content;
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class SaveMessageRequest {
        private Long sessionId;
        private Boolean isUser;
        private String content;
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public Boolean getIsUser() { return isUser; }
        public void setIsUser(Boolean isUser) { this.isUser = isUser; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
