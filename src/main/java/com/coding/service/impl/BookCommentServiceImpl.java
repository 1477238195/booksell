package com.coding.service.impl;

import com.coding.entity.BookComment;
import com.coding.mapper.BookCommentMapper;
import com.coding.service.IBookCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 书籍评论Service实现类
 */
@Service
public class BookCommentServiceImpl implements IBookCommentService {

    @Autowired
    private BookCommentMapper bookCommentMapper;

    @Override
    public List<BookComment> getCommentsByBookId(Long bookId) {
        if (bookId == null) {
            return new ArrayList<>();
        }
        return bookCommentMapper.selectByBookId(bookId);
    }

    @Override
    @Transactional
    public boolean addComment(Long bookId, Long userId, String content) {
        if (bookId == null || userId == null || content == null || content.trim().isEmpty()) {
            return false;
        }

        BookComment comment = new BookComment();
        comment.setBookId(bookId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setCreateTime(LocalDateTime.now());
        comment.setDeleted(0);

        return bookCommentMapper.insert(comment) > 0;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId, Integer userRole, Long bookSellerId) {
        if (commentId == null || userId == null) {
            return false;
        }
        
        // 只有管理员（role=2）、卖家（role=1且是书籍卖家）或评论所有者可以删除评论
        boolean canDelete = false;
        
        // 管理员可以删除任何评论
        if (userRole != null && userRole == 2) {
            canDelete = true;
        }
        // 卖家可以删除自己书籍的评论
        else if (userRole != null && userRole == 1 && bookSellerId != null && userId.equals(bookSellerId)) {
            canDelete = true;
        }
        // 用户可以删除自己的评论
        else {
            // 查询评论的所有者
            BookComment comment = bookCommentMapper.selectById(commentId);
            if (comment != null && userId.equals(comment.getUserId())) {
                canDelete = true;
            }
        }

        if (!canDelete) {
            return false;
        }

        return bookCommentMapper.deleteById(commentId) > 0;
    }

    @Override
    public int countComments(Long bookId) {
        if (bookId == null) {
            return 0;
        }
        return bookCommentMapper.countByBookId(bookId);
    }
}
