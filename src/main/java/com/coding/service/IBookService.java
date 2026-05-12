package com.coding.service;

import com.coding.entity.Book;
import com.coding.utils.PageResult;
import com.coding.utils.R;
import com.coding.utils.RequestPage;

/**
 * 书籍服务接口
 *
 * @author system
 * @since 2025-01-24
 */
public interface IBookService {

    /**
     * 分页查询书籍列表
     */
    PageResult<Book> page(String title, Long categoryId, Integer status, Long sellerId, RequestPage param);

    /**
     * 新增书籍
     */
    R<String> add(Book book);

    /**
     * 更新书籍
     */
    R<String> update(Book book);

    /**
     * 删除书籍
     */
    R<String> delete(Long bookId);

    /**
     * 根据ID查询书籍详情
     */
    R<Book> findById(Long bookId);

    /**
     * 上架/下架书籍
     */
    R<String> updateStatus(Long bookId, Integer status);
}
