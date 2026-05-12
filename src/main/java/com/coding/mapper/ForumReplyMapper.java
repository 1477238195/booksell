package com.coding.mapper;

import com.coding.entity.ForumReply;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ForumReplyMapper extends Mapper<ForumReply> {

    List<ForumReply> selectReplyListByTopicId(@Param("topicId") Long topicId);

    int softDeleteReply(@Param("replyId") Long replyId);

    int countByTopicId(@Param("topicId") Long topicId);
}
