package com.coding.service.impl;

import com.coding.entity.BookWanted;
import com.coding.entity.User;
import com.coding.mapper.BookWantedMapper;
import com.coding.mapper.UserMapper;
import com.coding.service.IBookWantedService;
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
 * 求购信息Service实现类
 *
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookWantedServiceImpl implements IBookWantedService {

    private final BookWantedMapper bookWantedMapper;
    private final UserMapper userMapper;

    private boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        User u = userMapper.selectByPrimaryKey(userId);
        return u != null && u.getRole() != null && Objects.equals(u.getRole(), 1);
    }

    @Override
    public PageResult<BookWanted> page(String bookTitle, Long userId, Integer status, RequestPage param) {
        PageHelper.startPage(param.getPage(), param.getSize());
        List<BookWanted> list = bookWantedMapper.selectWantedListWithDetails(bookTitle, userId, status);
        PageInfo<BookWanted> pageInfo = new PageInfo<>(list);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> add(BookWanted bookWanted) {
        try {
            Long userId = HttpKit.getUserId();
            if (userId == null) {
                return R.createByErrorMessage("未登录");
            }

            bookWanted.setUserId(userId);
            bookWanted.setCreateTime(LocalDateTime.now());
            bookWanted.setUpdateTime(LocalDateTime.now());
            bookWanted.setDeleted(0);

            if (bookWanted.getStatus() == null) {
                bookWanted.setStatus(1); // 默认求购中
            }

            bookWantedMapper.insertWanted(bookWanted);
            return R.createBySuccessMessage("发布成功");
        } catch (Exception e) {
            log.error("发布求购失败", e);
            return R.createByErrorMessage("发布失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> update(BookWanted bookWanted) {
        try {
            if (bookWanted.getWantedId() == null) {
                return R.createByErrorMessage("求购ID不能为空");
            }

            Long userId = HttpKit.getUserId();
            BookWanted existWanted = bookWantedMapper.selectByPrimaryKey(bookWanted.getWantedId());

            if (existWanted == null) {
                return R.createByErrorMessage("求购信息不存在");
            }

            if (!existWanted.getUserId().equals(userId) && !isAdmin(userId)) {
                return R.createByErrorMessage("无权限修改");
            }

            bookWanted.setUpdateTime(LocalDateTime.now());
            bookWantedMapper.updateByPrimaryKeySelective(bookWanted);
            return R.createBySuccessMessage("更新成功");
        } catch (Exception e) {
            log.error("更新求购失败", e);
            return R.createByErrorMessage("更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> delete(Long wantedId) {
        try {
            Long userId = HttpKit.getUserId();
            BookWanted bookWanted = bookWantedMapper.selectByPrimaryKey(wantedId);

            if (bookWanted == null) {
                return R.createByErrorMessage("求购信息不存在");
            }

            if (!bookWanted.getUserId().equals(userId) && !isAdmin(userId)) {
                return R.createByErrorMessage("无权限删除");
            }

            bookWanted.setDeleted(1);
            bookWanted.setUpdateTime(LocalDateTime.now());
            bookWantedMapper.updateByPrimaryKeySelective(bookWanted);
            return R.createBySuccessMessage("删除成功");
        } catch (Exception e) {
            log.error("删除求购失败", e);
            return R.createByErrorMessage("删除失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<BookWanted> findById(Long wantedId) {
        try {
            BookWanted bookWanted = bookWantedMapper.selectById(wantedId);
            if (bookWanted == null) {
                return R.createByErrorMessage("求购信息不存在");
            }
            
            // 增加浏览次数
            bookWantedMapper.increaseViewCount(wantedId);
            
            return R.createBySuccess(bookWanted);
        } catch (Exception e) {
            log.error("查询求购详情失败", e);
            return R.createByErrorMessage("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> updateStatus(Long wantedId, Integer status) {
        try {
            Long userId = HttpKit.getUserId();
            BookWanted bookWanted = bookWantedMapper.selectByPrimaryKey(wantedId);

            if (bookWanted == null) {
                return R.createByErrorMessage("求购信息不存在");
            }

            if (!bookWanted.getUserId().equals(userId) && !isAdmin(userId)) {
                return R.createByErrorMessage("无权限修改");
            }

            bookWanted.setStatus(status);
            bookWanted.setUpdateTime(LocalDateTime.now());
            bookWantedMapper.updateByPrimaryKeySelective(bookWanted);
            return R.createBySuccessMessage("状态更新成功");
        } catch (Exception e) {
            log.error("更新状态失败", e);
            return R.createByErrorMessage("更新失败：" + e.getMessage());
        }
    }
}
