package com.coding.service.impl;

import com.coding.entity.User;
import com.coding.mapper.UserMapper;
import com.coding.service.IUserService;
import com.coding.mybatis.utils.DomainUtil;
import com.coding.utils.HttpKit;
import com.coding.utils.R;
import org.springframework.stereotype.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.coding.utils.RequestPage;
import com.coding.utils.PageResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author roma@guanweiming.com
 * @since 2025-11-02
 */
@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;

    @Override
    public PageResult<User> page(Integer role, Integer status, @Validated RequestPage param) {
        PageHelper.startPage(param.getPage(),param.getSize(),DomainUtil.getKeyName(User.class) + " desc");

        User query = new User();
        if (role != null) {
            query.setRole(role);
        }
        if (status != null) {
            query.setStatus(status);
        }
        query.setDeleted(0);

        List<User> list = userMapper.select(query);
        PageInfo<User> pageInfo = new PageInfo<>(list);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public R<String> add(User user) {
        if (user == null) {
            return R.createByErrorMessage("添加失败");
        }
        user.setCreateTime(LocalDateTime.now());
        int resultCount = userMapper.insertSelective(user);
        if (resultCount == 0) {
            return R.createByErrorMessage("添加失败");
        }
        return R.createBySuccess();
    }

    @Override
    public R<String> delete(long id) {
        Long currentId = HttpKit.getUserId();
        User operator = userMapper.selectByPrimaryKey(currentId);
        boolean isAdmin = operator != null && Objects.equals(operator.getRole(), 1);
        if (!isAdmin) {
            return R.createByErrorMessage("无权删除用户");
        }
        if (Objects.equals(currentId, id)) {
            return R.createByErrorMessage("不能删除当前登录账号");
        }
        User target = userMapper.selectByPrimaryKey(id);
        if (target == null) {
            return R.createByErrorMessage("用户不存在");
        }
        if (Objects.equals(target.getRole(), 1)) {
            return R.createByErrorMessage("不能删除管理员账号");
        }
        int resultCount = userMapper.deleteByPrimaryKey(id);
        if (resultCount == 0) {
            return R.createByErrorMessage("删除失败");
        }
        return R.createBySuccess();
    }

    @Override
    public R<String> update(User user) {
        if (user.getUserId() == null) {
            return R.createByErrorMessage("传参有误，请检查");
        }
        Long currentId = HttpKit.getUserId();
        if (currentId == null) {
            return R.createByErrorMessage("请先登录");
        }
        User operator = userMapper.selectByPrimaryKey(currentId);
        boolean isAdmin = operator != null && operator.getRole() != null && Objects.equals(operator.getRole(), 1);
        if (!Objects.equals(currentId, user.getUserId()) && !isAdmin) {
            return R.createByErrorMessage("无权修改其他用户信息");
        }
        User record = userMapper.selectByPrimaryKey(user.getUserId());
        if (record == null) {
            return R.createByErrorMessage("更新失败，请检查");
        }
        if (!isAdmin) {
            // 非管理员仅允许改资料与头像，防止篡改角色、余额、密码等
            user.setUsername(null);
            user.setPassword(null);
            user.setRole(null);
            user.setStatus(null);
            user.setBalance(null);
            user.setCreateTime(null);
            user.setDeleted(null);
        }
        user.setUpdateTime(LocalDateTime.now());
        int resultCount = userMapper.updateByPrimaryKeySelective(user);
        if (resultCount == 0) {
            return R.createByErrorMessage("更新失败");
        }
        return R.createBySuccess();
    }

    @Override
    public R<User> findById(long id) {
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null) {
            return R.createByErrorMessage("查找失败");
        }
        return R.createBySuccess(user);
    }

    @Override
    public R<java.math.BigDecimal> getBalance() {
        try {
            Long userId = com.coding.utils.HttpKit.getUserId();
            if (userId == null) {
                return R.createByErrorMessage("未登录");
            }
            User user = userMapper.selectByPrimaryKey(userId);
            if (user == null) {
                return R.createByErrorMessage("用户不存在");
            }
            return R.createBySuccess(user.getBalance() != null ? user.getBalance() : java.math.BigDecimal.ZERO);
        } catch (Exception e) {
            log.error("获取余额失败", e);
            return R.createByErrorMessage("获取余额失败：" + e.getMessage());
        }
    }

}
