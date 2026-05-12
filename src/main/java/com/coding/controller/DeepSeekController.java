package com.coding.controller;

import com.coding.service.IDeepSeekService;
import com.coding.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * DeepSeek AI 控制器
 */
@Slf4j
@RequiredArgsConstructor
@Api(tags = "DeepSeek AI 服务")
@RequestMapping("/api/ai")
@RestController
public class DeepSeekController {
    
    private final IDeepSeekService deepSeekService;
    
    /**
     * 书籍推荐接口
     */
    @ApiOperation("书籍推荐")
    @PostMapping("/recommend")
    public R<String> recommendBooks(@RequestBody String preferences) {
        log.info("收到书籍推荐请求: {}", preferences);
        return deepSeekService.recommendBooks(preferences);
    }
    
    /**
     * 书籍讨论接口
     */
    @ApiOperation("书籍讨论")
    @PostMapping("/discuss")
    public R<String> discussBook(
            @RequestParam String bookName,
            @RequestBody String topic) {
        log.info("收到书籍讨论请求 - 书籍: {}, 主题: {}", bookName, topic);
        return deepSeekService.discussBook(bookName, topic);
    }
}
