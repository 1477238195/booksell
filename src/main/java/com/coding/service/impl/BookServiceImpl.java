package com.coding.service.impl;

import com.coding.entity.Book;
import com.coding.entity.User;
import com.coding.mapper.BookMapper;
import com.coding.mapper.UserMapper;
import com.coding.service.IBookService;
import com.coding.utils.HttpKit;
import com.coding.utils.PageResult;
import com.coding.utils.R;
import com.coding.utils.RequestPage;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 书籍服务实现类
 *
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements IBookService {

    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    private boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        User u = userMapper.selectByPrimaryKey(userId);
        return u != null && u.getRole() != null && Objects.equals(u.getRole(), 1);
    }

    @Override
    public PageResult<Book> page(String title, Long categoryId, Integer status, Long sellerId, RequestPage param) {
        PageHelper.startPage(param.getPage(), param.getSize());
        List<Book> list = bookMapper.selectBookListWithDetails(title, categoryId, status, sellerId);
        PageInfo<Book> pageInfo = new PageInfo<>(list);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> add(Book book) {
        try {
            Long userId = HttpKit.getUserId();
            if (userId == null) {
                return R.createByErrorMessage("未登录");
            }

            book.setSellerId(userId);
            book.setCreateTime(LocalDateTime.now());
            book.setUpdateTime(LocalDateTime.now());
            book.setDeleted(0);
            book.setViewCount(0);
            
            if (book.getStatus() == null) {
                book.setStatus(1); // 默认在售
            }
            if (book.getStock() == null) {
                book.setStock(1);
            }

            bookMapper.insertSelective(book);
            return R.createBySuccessMessage("发布成功");
        } catch (Exception e) {
            log.error("添加书籍失败", e);
            return R.createByErrorMessage("发布失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> update(Book book) {
        try {
            if (book.getBookId() == null) {
                return R.createByErrorMessage("书籍ID不能为空");
            }

            Long userId = HttpKit.getUserId();
            Book existBook = bookMapper.selectByPrimaryKey(book.getBookId());
            
            if (existBook == null) {
                return R.createByErrorMessage("书籍不存在");
            }
            
            if (!existBook.getSellerId().equals(userId) && !isAdmin(userId)) {
                return R.createByErrorMessage("无权限修改");
            }

            book.setUpdateTime(LocalDateTime.now());
            bookMapper.updateByPrimaryKeySelective(book);
            return R.createBySuccessMessage("更新成功");
        } catch (Exception e) {
            log.error("更新书籍失败", e);
            return R.createByErrorMessage("更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> delete(Long bookId) {
        try {
            Long userId = HttpKit.getUserId();
            Book book = bookMapper.selectByPrimaryKey(bookId);
            
            if (book == null) {
                return R.createByErrorMessage("书籍不存在");
            }
            
            if (!book.getSellerId().equals(userId) && !isAdmin(userId)) {
                return R.createByErrorMessage("无权限删除");
            }

            // 逻辑删除
            book.setDeleted(1);
            book.setUpdateTime(LocalDateTime.now());
            bookMapper.updateByPrimaryKeySelective(book);
            return R.createBySuccessMessage("删除成功");
        } catch (Exception e) {
            log.error("删除书籍失败", e);
            return R.createByErrorMessage("删除失败：" + e.getMessage());
        }
    }

    @Override
    public R<Book> findById(Long bookId) {
        try {
            Book book = bookMapper.selectByPrimaryKey(bookId);
            if (book == null || book.getDeleted() == 1) {
                return R.createByErrorMessage("书籍不存在");
            }
            
            // 增加浏览次数
            bookMapper.increaseViewCount(bookId);
            
            return R.createBySuccess(book);
        } catch (Exception e) {
            log.error("查询书籍失败", e);
            return R.createByErrorMessage("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> updateStatus(Long bookId, Integer status) {
        try {
            Long userId = HttpKit.getUserId();
            Book book = bookMapper.selectByPrimaryKey(bookId);
            
            if (book == null) {
                return R.createByErrorMessage("书籍不存在");
            }
            
            if (!book.getSellerId().equals(userId) && !isAdmin(userId)) {
                return R.createByErrorMessage("无权限操作");
            }

            book.setStatus(status);
            book.setUpdateTime(LocalDateTime.now());
            bookMapper.updateByPrimaryKeySelective(book);
            
            String statusText = status == 1 ? "上架" : "下架";
            return R.createBySuccessMessage(statusText + "成功");
        } catch (Exception e) {
            log.error("更新书籍状态失败", e);
            return R.createByErrorMessage("操作失败：" + e.getMessage());
        }
    }
}
