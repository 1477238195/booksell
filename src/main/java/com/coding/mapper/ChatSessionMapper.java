package com.coding.mapper;

import com.coding.entity.ChatSession;
import tk.mybatis.mapper.common.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatSessionMapper extends Mapper<ChatSession> {
    List<ChatSession> selectByUserId(@Param("userId") Long userId);
}
