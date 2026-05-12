package com.coding.controller;

import com.coding.common.Const;
import com.coding.entity.ForumBoard;
import com.coding.entity.ForumReply;
import com.coding.entity.ForumTopic;
import com.coding.service.IForumService;
import com.coding.utils.PageResult;
import com.coding.utils.R;
import com.coding.utils.RequestPage;
import com.coding.vo.ForumTopicDetailDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "论坛")
@RestController
@RequestMapping(Const.API + "forum")
@RequiredArgsConstructor
public class ForumController {

    private final IForumService forumService;

    @ApiOperation("版块列表")
    @GetMapping("boards")
    public R<List<ForumBoard>> boards() {
        return R.createBySuccess(forumService.listBoards());
    }

    @ApiOperation("主题分页")
    @GetMapping("topic/page")
    public PageResult<ForumTopic> topicPage(
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long bookId,
            @Validated RequestPage param) {
        return forumService.pageTopics(boardId, keyword, bookId, param);
    }

    @ApiOperation("主题详情（含回复分页）")
    @GetMapping("topic/detail")
    public R<ForumTopicDetailDto> topicDetail(
            @RequestParam Long topicId,
            @Validated RequestPage replyPage) {
        return forumService.topicDetail(topicId, replyPage);
    }

    @ApiOperation("发帖")
    @PostMapping("topic/add")
    public R<String> addTopic(@RequestBody ForumTopic body) {
        return forumService.addTopic(body);
    }

    @ApiOperation("编辑主题")
    @PostMapping("topic/update")
    public R<String> updateTopic(@RequestBody ForumTopic body) {
        return forumService.updateTopic(body);
    }

    @ApiOperation("删除主题")
    @GetMapping("topic/delete")
    public R<String> deleteTopic(@RequestParam Long topicId) {
        return forumService.deleteTopic(topicId);
    }

    @ApiOperation("回复")
    @PostMapping("reply/add")
    public R<String> addReply(@RequestBody ForumReply body) {
        return forumService.addReply(body);
    }

    @ApiOperation("删除回复")
    @GetMapping("reply/delete")
    public R<String> deleteReply(@RequestParam Long replyId) {
        return forumService.deleteReply(replyId);
    }
}
