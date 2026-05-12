package com.coding.service.impl;

import com.coding.entity.BookCategory;
import com.coding.mapper.BookCategoryMapper;
import com.coding.service.IBookCategoryService;
import com.coding.utils.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 书籍分类服务实现类
 *
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookCategoryServiceImpl implements IBookCategoryService {

    private final BookCategoryMapper bookCategoryMapper;

    @Override
    public R<List<BookCategory>> list() {
        try {
            List<BookCategory> list = bookCategoryMapper.selectAll();
            return R.createBySuccess(list);
        } catch (Exception e) {
            log.error("查询分类失败", e);
            return R.createByErrorMessage("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> add(BookCategory category) {
        try {
            category.setCreateTime(LocalDateTime.now());
            if (category.getParentId() == null) {
                category.setParentId(0L);
            }
            if (category.getStatus() == null) {
                category.setStatus(1);
            }
            bookCategoryMapper.insertSelective(category);
            return R.createBySuccessMessage("添加成功");
        } catch (Exception e) {
            log.error("添加分类失败", e);
            return R.createByErrorMessage("添加失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> update(BookCategory category) {
        try {
            if (category.getCategoryId() == null) {
                return R.createByErrorMessage("分类ID不能为空");
            }
            bookCategoryMapper.updateByPrimaryKeySelective(category);
            return R.createBySuccessMessage("更新成功");
        } catch (Exception e) {
            log.error("更新分类失败", e);
            return R.createByErrorMessage("更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> delete(Long categoryId) {
        try {
            bookCategoryMapper.deleteByPrimaryKey(categoryId);
            return R.createBySuccessMessage("删除成功");
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return R.createByErrorMessage("删除失败：" + e.getMessage());
        }
    }
}
