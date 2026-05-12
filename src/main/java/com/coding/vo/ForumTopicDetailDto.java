package com.coding.vo;

import com.coding.entity.ForumReply;
import com.coding.entity.ForumTopic;
import lombok.Data;

import java.util.List;

@Data
public class ForumTopicDetailDto {
    private ForumTopic topic;
    private Long replyTotal;
    private List<ForumReply> replies;
}
