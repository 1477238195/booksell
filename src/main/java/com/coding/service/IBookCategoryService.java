package com.coding.service;

import com.coding.entity.BookCategory;
import com.coding.utils.R;

import java.util.List;

/**
 * 书籍分类服务接口
 *
 * @author system
 * @since 2025-01-24
 */
public interface IBookCategoryService {

    /**
     * 查询所有分类
     */
    R<List<BookCategory>> list();

    /**
     * 新增分类
     */
    R<String> add(BookCategory category);

    /**
     * 更新分类
     */
    R<String> update(BookCategory category);

    /**
     * 删除分类
     */
    R<String> delete(Long categoryId);
}
