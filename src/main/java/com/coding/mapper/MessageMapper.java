package com.coding.mapper;

import com.coding.entity.Message;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface MessageMapper extends Mapper<Message> {
    List<Message> selectMessageListWithDetails(@Param("userId") Long userId);
    int countUnreadMessages(@Param("userId") Long userId);
    List<Message> selectConversationWithDetails(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);
}
