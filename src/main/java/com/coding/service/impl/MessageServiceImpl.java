package com.coding.service.impl;

import com.coding.entity.Message;
import com.coding.mapper.MessageMapper;
import com.coding.service.IMessageService;
import com.coding.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {
    private final MessageMapper messageMapper;

    @Override
    public PageResult<Message> page(RequestPage param) {
        Long userId = HttpKit.getUserId();
        PageHelper.startPage(param.getPage(), param.getSize());
        List<Message> list = messageMapper.selectMessageListWithDetails(userId);
        PageInfo<Message> pageInfo = new PageInfo<>(list);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> sendMessage(Message message) {
        try {
            Long userId = HttpKit.getUserId();
            message.setSenderId(userId);
            message.setCreateTime(LocalDateTime.now());
            message.setDeleted(0);
            message.setIsRead(0);
            if (message.getType() == null) message.setType(1);
            messageMapper.insertSelective(message);
            return R.createBySuccessMessage("发送成功");
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return R.createByErrorMessage("发送失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> markAsRead(Long messageId) {
        try {
            Long userId = HttpKit.getUserId();
            Message message = messageMapper.selectByPrimaryKey(messageId);
            if (message == null) {
                return R.createByErrorMessage("消息不存在");
            }
            if (!Objects.equals(message.getReceiverId(), userId)) {
                return R.createByErrorMessage("仅接收者可标记已读");
            }
            if (message.getIsRead() != null && message.getIsRead() == 1) {
                return R.createBySuccessMessage("已是已读");
            }
            message.setIsRead(1);
            messageMapper.updateByPrimaryKeySelective(message);
            return R.createBySuccessMessage("标记成功");
        } catch (Exception e) {
            return R.createByErrorMessage("操作失败");
        }
    }

    @Override
    public R<Integer> getUnreadCount() {
        Long userId = HttpKit.getUserId();
        int count = messageMapper.countUnreadMessages(userId);
        return R.createBySuccess(count);
    }

    @Override
    public PageResult<Message> getConversation(Long targetUserId, RequestPage param) {
        Long userId = HttpKit.getUserId();
        PageHelper.startPage(param.getPage(), param.getSize());
        List<Message> list = messageMapper.selectConversationWithDetails(userId, targetUserId);
        PageInfo<Message> pageInfo = new PageInfo<>(list);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());
    }
}
