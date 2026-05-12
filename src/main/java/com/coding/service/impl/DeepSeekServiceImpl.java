package com.coding.service.impl;

import com.coding.config.DeepSeekConfig;
import com.coding.dto.deepseek.DeepSeekRequest;
import com.coding.dto.deepseek.DeepSeekResponse;
import com.coding.service.IDeepSeekService;
import com.coding.utils.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * DeepSeek AI 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekServiceImpl implements IDeepSeekService {
    
    private final DeepSeekConfig deepSeekConfig;
    private final RestTemplate restTemplate;
    
    @Override
    public R<String> chat(String userMessage) {
        log.info("开始调用DeepSeek API，用户消息: {}", userMessage);
        try {
            String systemPrompt = "你是「阅谈」二手图书交易平台的AI书伴助手。你是一位热爱书籍、懂书惜书的朋友，熟悉各类图书的品相鉴定、市场行情，能帮助用户：\n" +
                    "1. 找到想要的二手书，评估价格是否合理\n" +
                    "2. 了解书籍的保存状态和价值\n" +
                    "3. 推荐适合的书籍\n" +
                    "4. 分享读书心得和书籍背后的故事\n" +
                    "5. 解答关于二手书交易的各类问题\n" +
                    "请用亲切、专业的语气与用户交流，就像一位书斋里的老朋友。";

            DeepSeekRequest request = DeepSeekRequest.builder()
                    .model(deepSeekConfig.getModel())
                    .temperature(deepSeekConfig.getTemperature())
                    .max_tokens(deepSeekConfig.getMaxTokens())
                    .messages(Arrays.asList(
                            DeepSeekRequest.Message.builder()
                                    .role("system")
                                    .content(systemPrompt)
                                    .build(),
                            DeepSeekRequest.Message.builder()
                                    .role("user")
                                    .content(userMessage)
                                    .build()
                    ))
                    .build();
            
            log.info("请求模型: {}, 温度: {}, maxTokens: {}", deepSeekConfig.getModel(), deepSeekConfig.getTemperature(), deepSeekConfig.getMaxTokens());
            
            DeepSeekResponse response = callDeepSeekApi(request);
            
            log.info("DeepSeek响应: {}", response);
            
            if (response == null) {
                log.error("DeepSeek API返回null");
                return R.createByErrorMessage("API返回为空");
            }
            
            if (response.getChoices() == null) {
                log.error("DeepSeek响应中choices为空");
                return R.createByErrorMessage("响应格式错误：缺少choices字段");
            }
            
            if (response.getChoices().isEmpty()) {
                log.error("DeepSeek响应中choices列表为空");
                return R.createByErrorMessage("响应为空");
            }
            
            DeepSeekRequest.Message message = response.getChoices().get(0).getMessage();
            if (message == null) {
                log.error("DeepSeek响应中message为空");
                return R.createByErrorMessage("响应格式错误：缺少message字段");
            }
            
            String content = message.getContent();
            if (content == null || content.trim().isEmpty()) {
                log.error("DeepSeek响应内容为空");
                return R.createByErrorMessage("响应内容为空");
            }
            
            log.info("DeepSeek 响应成功，内容长度: {}，使用token: {}", content.length(), response.getUsage() != null ? response.getUsage().getTotal_tokens() : 0);
            return R.createBySuccess(content);
        } catch (Exception e) {
            log.error("DeepSeek API调用失败", e);
            return R.createByErrorMessage("调用失败：" + e.getMessage());
        }
    }
    
    @Override
    public R<String> recommendBooks(String userPreferences) {
        String prompt = String.format(
            "作为一位专业的书籍推荐助手，请根据用户的阅读偏好推荐合适的书籍。\n\n" +
            "用户偏好：%s\n\n" +
            "请推荐5本适合的书籍，并按照以下格式输出：\n\n" +
            "1. 《书名》- 作者\n" +
            "   推荐理由：简要说明为什么这本书适合用户\n" +
            "   适合人群：适合什么样的读者\n\n" +
            "2. 《书名》- 作者\n" +
            "   推荐理由：简要说明为什么这本书适合用户\n" +
            "   适合人群：适合什么样的读者\n\n" +
            "...", 
            userPreferences);
        
        return chat(prompt);
    }
    
    @Override
    public R<String> discussBook(String bookName, String discussionTopic) {
        String prompt = String.format(
            "让我们来讨论一下《%s》这本书。\n\n" +
            "讨论主题：%s\n\n" +
            "请从以下几个方面进行深入分析：\n" +
            "1. 书籍的核心主题和思想\n" +
            "2. 主要人物和角色塑造\n" +
            "3. 写作风格和叙事手法\n" +
            "4. 您对这个主题的看法和见解\n\n" +
            "请用通俗易懂的语言进行分析，避免过于学术化的表达。", 
            bookName, discussionTopic);
        
        return chat(prompt);
    }
    
    /**
     * 调用 DeepSeek API
     */
    private DeepSeekResponse callDeepSeekApi(DeepSeekRequest request) {
        String url = deepSeekConfig.getBaseUrl() + "/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepSeekConfig.getApiKey());
        
        HttpEntity<DeepSeekRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<DeepSeekResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                DeepSeekResponse.class
        );
        
        return response.getBody();
    }
}
