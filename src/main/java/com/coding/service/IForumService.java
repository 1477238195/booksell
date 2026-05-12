package com.coding.service;

import com.coding.entity.ForumBoard;
import com.coding.entity.ForumReply;
import com.coding.entity.ForumTopic;
import com.coding.utils.PageResult;
import com.coding.utils.R;
import com.coding.utils.RequestPage;
import com.coding.vo.ForumTopicDetailDto;

import java.util.List;

public interface IForumService {

    List<ForumBoard> listBoards();

    PageResult<ForumTopic> pageTopics(Long boardId, String keyword, Long bookId, RequestPage param);

    R<ForumTopicDetailDto> topicDetail(Long topicId, RequestPage replyPage);

    R<String> addTopic(ForumTopic body);

    R<String> updateTopic(ForumTopic body);

    R<String> deleteTopic(Long topicId);

    R<String> addReply(ForumReply body);

    R<String> deleteReply(Long replyId);
}
