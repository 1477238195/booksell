package com.coding.service;

import com.coding.entity.Message;
import com.coding.utils.PageResult;
import com.coding.utils.R;
import com.coding.utils.RequestPage;

public interface IMessageService {
    PageResult<Message> page(RequestPage param);
    R<String> sendMessage(Message message);
    R<String> markAsRead(Long messageId);
    R<Integer> getUnreadCount();
    PageResult<Message> getConversation(Long targetUserId, RequestPage param);
}
