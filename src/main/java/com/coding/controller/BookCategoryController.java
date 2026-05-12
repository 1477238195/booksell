package com.coding.controller;

import com.coding.common.Const;
import com.coding.entity.BookCategory;
import com.coding.service.IBookCategoryService;
import com.coding.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 书籍分类控制器
 *
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Api(tags = "书籍分类管理")
@RequestMapping(Const.API + "bookCategory")
@RequiredArgsConstructor
@RestController
public class BookCategoryController {

    private final IBookCategoryService bookCategoryService;

    @ApiOperation("查询所有分类")
    @GetMapping("list")
    public R<List<BookCategory>> list() {
        return bookCategoryService.list();
    }

    @ApiOperation("新增分类")
    @PostMapping("add")
    public R<String> add(@RequestBody @Valid BookCategory category) {
        return bookCategoryService.add(category);
    }

    @ApiOperation("更新分类")
    @PostMapping("update")
    public R<String> update(@RequestBody @Valid BookCategory category) {
        return bookCategoryService.update(category);
    }

    @ApiOperation("删除分类")
    @GetMapping("delete")
    public R<String> delete(@RequestParam Long categoryId) {
        return bookCategoryService.delete(categoryId);
    }
}
