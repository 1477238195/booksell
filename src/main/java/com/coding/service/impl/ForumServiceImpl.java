package com.coding.service.impl;

import com.coding.entity.*;
import com.coding.mapper.BookMapper;
import com.coding.mapper.ForumBoardMapper;
import com.coding.mapper.ForumReplyMapper;
import com.coding.mapper.ForumTopicMapper;
import com.coding.mapper.UserMapper;
import com.coding.service.IForumService;
import com.coding.utils.HttpKit;
import com.coding.utils.PageResult;
import com.coding.utils.R;
import com.coding.utils.RequestPage;
import com.coding.vo.ForumTopicDetailDto;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements IForumService {

    private final ForumBoardMapper forumBoardMapper;
    private final ForumTopicMapper forumTopicMapper;
    private final ForumReplyMapper forumReplyMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    private boolean isAdmin(Long userId) {
        if (userId == null) return false;
        User u = userMapper.selectByPrimaryKey(userId);
        return u != null && u.getRole() != null && Objects.equals(u.getRole(), 1);
    }

    @Override
    public List<ForumBoard> listBoards() {
        Example ex = new Example(ForumBoard.class);
        ex.createCriteria().andEqualTo("status", 1);
        ex.setOrderByClause("sort_order ASC, board_id ASC");
        return forumBoardMapper.selectByExample(ex);
    }

    @Override
    public PageResult<ForumTopic> pageTopics(Long boardId, String keyword, Long bookId, RequestPage param) {
        PageHelper.startPage(param.getPage(), param.getSize());
        List<ForumTopic> list = forumTopicMapper.selectTopicList(boardId, keyword, bookId);
        PageInfo<ForumTopic> pageInfo = new PageInfo<>(list);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public R<ForumTopicDetailDto> topicDetail(Long topicId, RequestPage replyPage) {
        ForumTopic topic = forumTopicMapper.selectTopicById(topicId);
        if (topic == null) {
            return R.createByErrorMessage("主题不存在或已删除");
        }
        forumTopicMapper.increaseViewCount(topicId);
        topic.setViewCount(topic.getViewCount() == null ? 1 : topic.getViewCount() + 1);

        PageHelper.startPage(replyPage.getPage(), replyPage.getSize());
        List<ForumReply> replies = forumReplyMapper.selectReplyListByTopicId(topicId);
        PageInfo<ForumReply> pageInfo = new PageInfo<>(replies);

        ForumTopicDetailDto dto = new ForumTopicDetailDto();
        dto.setTopic(topic);
        dto.setReplyTotal(pageInfo.getTotal());
        dto.setReplies(pageInfo.getList());
        return R.createBySuccess(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> addTopic(ForumTopic body) {
        Long userId = HttpKit.getUserId();
        if (userId == null) {
            return R.createByErrorMessage("请先登录");
        }
        if (body.getBoardId() == null || body.getTitle() == null || body.getTitle().trim().isEmpty()) {
            return R.createByErrorMessage("版块与标题不能为空");
        }
        if (body.getContent() == null || body.getContent().trim().isEmpty()) {
            return R.createByErrorMessage("正文不能为空");
        }
        ForumBoard board = forumBoardMapper.selectByPrimaryKey(body.getBoardId());
        if (board == null || !Objects.equals(board.getStatus(), 1)) {
            return R.createByErrorMessage("版块不存在");
        }
        if (body.getBookId() != null) {
            Book book = bookMapper.selectByPrimaryKey(body.getBookId());
            if (book == null || (book.getDeleted() != null && book.getDeleted() == 1)) {
                return R.createByErrorMessage("关联书籍不存在");
            }
        }
        body.setUserId(userId);
        body.setTitle(body.getTitle().trim());
        body.setViewCount(0);
        body.setReplyCount(0);
        body.setPinned(0);
        body.setLocked(0);
        body.setDeleted(0);
        body.setCreateTime(LocalDateTime.now());
        body.setUpdateTime(LocalDateTime.now());
        forumTopicMapper.insertSelective(body);
        return R.createBySuccessMessage("发布成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> updateTopic(ForumTopic body) {
        if (body.getTopicId() == null) {
            return R.createByErrorMessage("主题ID不能为空");
        }
        Long userId = HttpKit.getUserId();
        if (userId == null) {
            return R.createByErrorMessage("请先登录");
        }
        ForumTopic exist = forumTopicMapper.selectByPrimaryKey(body.getTopicId());
        if (exist == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
            return R.createByErrorMessage("主题不存在");
        }
        if (!exist.getUserId().equals(userId) && !isAdmin(userId)) {
            return R.createByErrorMessage("无权限修改");
        }
        if (body.getBookId() != null) {
            Book book = bookMapper.selectByPrimaryKey(body.getBookId());
            if (book == null || (book.getDeleted() != null && book.getDeleted() == 1)) {
                return R.createByErrorMessage("关联书籍不存在");
            }
        }
        ForumTopic upd = new ForumTopic();
        upd.setTopicId(body.getTopicId());
        if (body.getTitle() != null) {
            upd.setTitle(body.getTitle().trim());
        }
        if (body.getContent() != null) {
            upd.setContent(body.getContent());
        }
        if (body.getBookId() != null) {
            upd.setBookId(body.getBookId());
        }
        upd.setUpdateTime(LocalDateTime.now());
        forumTopicMapper.updateByPrimaryKeySelective(upd);
        return R.createBySuccessMessage("更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> deleteTopic(Long topicId) {
        Long userId = HttpKit.getUserId();
        if (userId == null) {
            return R.createByErrorMessage("请先登录");
        }
        ForumTopic exist = forumTopicMapper.selectByPrimaryKey(topicId);
        if (exist == null) {
            return R.createByErrorMessage("主题不存在");
        }
        if (!exist.getUserId().equals(userId) && !isAdmin(userId)) {
            return R.createByErrorMessage("无权限删除");
        }
        forumTopicMapper.softDeleteTopic(topicId);
        return R.createBySuccessMessage("已删除");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> addReply(ForumReply body) {
        Long userId = HttpKit.getUserId();
        if (userId == null) {
            return R.createByErrorMessage("请先登录");
        }
        if (body.getTopicId() == null || body.getContent() == null || body.getContent().trim().isEmpty()) {
            return R.createByErrorMessage("主题与回复内容不能为空");
        }
        ForumTopic topic = forumTopicMapper.selectByPrimaryKey(body.getTopicId());
        if (topic == null || (topic.getDeleted() != null && topic.getDeleted() == 1)) {
            return R.createByErrorMessage("主题不存在");
        }
        if (topic.getLocked() != null && topic.getLocked() == 1 && !isAdmin(userId)) {
            return R.createByErrorMessage("主题已锁定");
        }
        body.setUserId(userId);
        body.setContent(body.getContent().trim());
        body.setDeleted(0);
        body.setCreateTime(LocalDateTime.now());
        body.setUpdateTime(LocalDateTime.now());
        forumReplyMapper.insertSelective(body);

        ForumTopic upd = new ForumTopic();
        upd.setTopicId(topic.getTopicId());
        upd.setReplyCount((topic.getReplyCount() == null ? 0 : topic.getReplyCount()) + 1);
        upd.setLastReplyAt(LocalDateTime.now());
        upd.setLastReplyUserId(userId);
        upd.setUpdateTime(LocalDateTime.now());
        forumTopicMapper.updateByPrimaryKeySelective(upd);
        return R.createBySuccessMessage("回复成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> deleteReply(Long replyId) {
        Long userId = HttpKit.getUserId();
        if (userId == null) {
            return R.createByErrorMessage("请先登录");
        }
        ForumReply reply = forumReplyMapper.selectByPrimaryKey(replyId);
        if (reply == null) {
            return R.createByErrorMessage("回复不存在");
        }
        if (!reply.getUserId().equals(userId) && !isAdmin(userId)) {
            return R.createByErrorMessage("无权限删除");
        }
        forumReplyMapper.softDeleteReply(replyId);

        long cnt = forumReplyMapper.countByTopicId(reply.getTopicId());
        ForumTopic upd = new ForumTopic();
        upd.setTopicId(reply.getTopicId());
        upd.setReplyCount((int) cnt);
        upd.setUpdateTime(LocalDateTime.now());
        forumTopicMapper.updateByPrimaryKeySelective(upd);
        return R.createBySuccessMessage("已删除");
    }
}
