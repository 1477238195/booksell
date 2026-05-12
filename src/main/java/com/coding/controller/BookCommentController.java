package com.coding.controller;

import com.coding.entity.BookComment;
import com.coding.service.IBookCommentService;
import com.coding.utils.HttpKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 书籍评论Controller
 */
@RestController
@RequestMapping("/api/book/comment")
public class BookCommentController {

    @Autowired
    private IBookCommentService bookCommentService;

    /**
     * 根据书籍ID获取评论列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getComments(@RequestParam Long bookId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<BookComment> comments = bookCommentService.getCommentsByBookId(bookId);
            result.put("status", 0);
            result.put("data", comments);
            result.put("count", comments.size());
        } catch (Exception e) {
            result.put("status", -1);
            result.put("msg", "获取评论失败");
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 添加评论
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addComment(
            @RequestParam Long bookId,
            @RequestParam String content) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            // 使用 HttpKit 获取用户ID
            Long userId = HttpKit.getUserId();
            if (userId == null) {
                result.put("status", -1);
                result.put("msg", "请先登录");
                return ResponseEntity.ok(result);
            }

            boolean success = bookCommentService.addComment(bookId, userId, content);
            if (success) {
                result.put("status", 0);
                result.put("msg", "评论成功");
            } else {
                result.put("status", -1);
                result.put("msg", "评论失败");
            }
        } catch (Exception e) {
            result.put("status", -1);
            result.put("msg", "评论失败：" + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 删除评论
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @RequestParam Long commentId,
            @RequestParam(required = false) Long bookSellerId) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            // 使用 HttpKit 获取用户ID
            Long userId = HttpKit.getUserId();
            if (userId == null) {
                result.put("status", -1);
                result.put("msg", "请先登录");
                return ResponseEntity.ok(result);
            }

            boolean success = bookCommentService.deleteComment(
                    commentId, 
                    userId, 
                    null, 
                    bookSellerId
            );
            if (success) {
                result.put("status", 0);
                result.put("msg", "删除成功");
            } else {
                result.put("status", -1);
                result.put("msg", "无权限删除此评论");
            }
        } catch (Exception e) {
            result.put("status", -1);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
