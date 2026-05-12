package com.coding.controller;

import com.coding.common.Const;
import com.coding.entity.Message;
import com.coding.service.IMessageService;
import com.coding.utils.*;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "站内消息管理")
@RequestMapping(Const.API + "message")
@RequiredArgsConstructor
@RestController
public class MessageController {
    private final IMessageService messageService;

    @ApiOperation("分页查询消息列表")
    @GetMapping("page")
    public PageResult<Message> page(@Validated RequestPage param) {
        return messageService.page(param);
    }

    @ApiOperation("发送消息")
    @PostMapping("send")
    public R<String> sendMessage(@RequestBody Message message) {
        return messageService.sendMessage(message);
    }

    @ApiOperation("标记为已读")
    @PostMapping("markAsRead")
    public R<String> markAsRead(@RequestParam Long messageId) {
        return messageService.markAsRead(messageId);
    }

    @ApiOperation("获取未读消息数量")
    @GetMapping("unreadCount")
    public R<Integer> getUnreadCount() {
        return messageService.getUnreadCount();
    }

    @ApiOperation("获取与指定用户的对话列表")
    @GetMapping("conversation")
    public PageResult<Message> getConversation(
            @RequestParam Long targetUserId,
            @Validated RequestPage param) {
        return messageService.getConversation(targetUserId, param);
    }
}
