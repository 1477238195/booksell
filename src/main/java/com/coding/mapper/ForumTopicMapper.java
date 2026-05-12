package com.coding.mapper;

import com.coding.entity.ForumTopic;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ForumTopicMapper extends Mapper<ForumTopic> {

    List<ForumTopic> selectTopicList(@Param("boardId") Long boardId,
                                     @Param("keyword") String keyword,
                                     @Param("bookId") Long bookId);

    ForumTopic selectTopicById(@Param("topicId") Long topicId);

    int increaseViewCount(@Param("topicId") Long topicId);

    int softDeleteTopic(@Param("topicId") Long topicId);
}
