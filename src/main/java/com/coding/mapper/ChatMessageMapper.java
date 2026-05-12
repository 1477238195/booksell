package com.coding.mapper;

import com.coding.entity.ChatMessage;
import tk.mybatis.mapper.common.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageMapper extends Mapper<ChatMessage> {
    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId);
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}
