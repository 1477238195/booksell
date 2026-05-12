package com.coding.service.impl;

import com.coding.entity.Book;
import com.coding.entity.Order;
import com.coding.entity.User;
import com.coding.mapper.BookMapper;
import com.coding.mapper.OrderMapper;
import com.coding.mapper.UserMapper;
import com.coding.service.IOrderService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单Service实现类
 *
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderMapper orderMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<Order> page(Long buyerId, Long sellerId, Integer status, RequestPage param) {
        PageHelper.startPage(param.getPage(), param.getSize());
        List<Order> list = orderMapper.selectOrderListWithDetails(buyerId, sellerId, status);
        PageInfo<Order> pageInfo = new PageInfo<>(list);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> createOrder(Long bookId, Integer quantity) {
        try {
            Long userId = HttpKit.getUserId();
            if (userId == null) {
                return R.createByErrorMessage("未登录");
            }

            // 查询用户信息
            User buyer = userMapper.selectByPrimaryKey(userId);
            if (buyer == null) {
                return R.createByErrorMessage("用户不存在");
            }

            // 查询书籍信息
            Book book = bookMapper.selectByPrimaryKey(bookId);
            if (book == null) {
                return R.createByErrorMessage("书籍不存在");
            }

            if (book.getStatus() != 1) {
                return R.createByErrorMessage("书籍已下架");
            }

            if (book.getStock() < quantity) {
                return R.createByErrorMessage("库存不足");
            }

            if (book.getSellerId().equals(userId)) {
                return R.createByErrorMessage("不能购买自己的书籍");
            }

            // 计算订单总价
            BigDecimal totalPrice = book.getPrice().multiply(new BigDecimal(quantity));
            
            // 检查余额是否充足
            if (buyer.getBalance() == null || buyer.getBalance().compareTo(totalPrice) < 0) {
                return R.createByErrorMessage("二手币余额不足，当前余额：" + 
                    (buyer.getBalance() != null ? buyer.getBalance() : "0") + "，需要：" + totalPrice);
            }

            // 生成订单编号
            String orderNo = generateOrderNo();

            // 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setBuyerId(userId);
            order.setSellerId(book.getSellerId());
            order.setBookId(bookId);
            order.setQuantity(quantity);
            order.setTotalPrice(totalPrice);
            order.setStatus(1); // 待支付
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            order.setDeleted(0);

            orderMapper.insertSelective(order);

            // 减库存
            book.setStock(book.getStock() - quantity);
            bookMapper.updateByPrimaryKeySelective(book);

            return R.createBySuccessMessage("下单成功");
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return R.createByErrorMessage("下单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> cancelOrder(Long orderId) {
        try {
            Long userId = HttpKit.getUserId();
            Order order = orderMapper.selectByPrimaryKey(orderId);

            if (order == null) {
                return R.createByErrorMessage("订单不存在");
            }

            // 只有买家或卖家可以取消订单
            if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
                return R.createByErrorMessage("无权限操作");
            }

            // 只有待支付和待发货的订单可以取消
            if (order.getStatus() != 1 && order.getStatus() != 2) {
                return R.createByErrorMessage("订单状态不允许取消");
            }

            // 如果已支付，需要退款
            if (order.getStatus() == 2) {
                User buyer = userMapper.selectByPrimaryKey(order.getBuyerId());
                User seller = userMapper.selectByPrimaryKey(order.getSellerId());
                
                if (buyer != null && seller != null) {
                    // 退款给买家
                    if (buyer.getBalance() == null) {
                        buyer.setBalance(BigDecimal.ZERO);
                    }
                    buyer.setBalance(buyer.getBalance().add(order.getTotalPrice()));
                    userMapper.updateByPrimaryKeySelective(buyer);
                    
                    // 扣除卖家余额
                    if (seller.getBalance() != null && seller.getBalance().compareTo(order.getTotalPrice()) >= 0) {
                        seller.setBalance(seller.getBalance().subtract(order.getTotalPrice()));
                        userMapper.updateByPrimaryKeySelective(seller);
                    }
                }
            }

            order.setStatus(5); // 已取消
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateByPrimaryKeySelective(order);

            // 恢复库存
            Book book = bookMapper.selectByPrimaryKey(order.getBookId());
            if (book != null) {
                book.setStock(book.getStock() + order.getQuantity());
                bookMapper.updateByPrimaryKeySelective(book);
            }

            return R.createBySuccessMessage("取消成功");
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return R.createByErrorMessage("取消失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> payOrder(Long orderId) {
        try {
            Long userId = HttpKit.getUserId();
            Order order = orderMapper.selectByPrimaryKey(orderId);

            if (order == null) {
                return R.createByErrorMessage("订单不存在");
            }

            if (!order.getBuyerId().equals(userId)) {
                return R.createByErrorMessage("无权限操作");
            }

            if (order.getStatus() != 1) {
                return R.createByErrorMessage("订单状态不正确");
            }

            // 查询买家和卖家信息
            User buyer = userMapper.selectByPrimaryKey(order.getBuyerId());
            User seller = userMapper.selectByPrimaryKey(order.getSellerId());

            if (buyer == null || seller == null) {
                return R.createByErrorMessage("用户信息不存在");
            }

            // 检查余额
            if (buyer.getBalance() == null || buyer.getBalance().compareTo(order.getTotalPrice()) < 0) {
                return R.createByErrorMessage("二手币余额不足，当前余额：" + 
                    (buyer.getBalance() != null ? buyer.getBalance() : "0"));
            }

            // 扣除买家余额
            buyer.setBalance(buyer.getBalance().subtract(order.getTotalPrice()));
            userMapper.updateByPrimaryKeySelective(buyer);

            // 增加卖家余额
            if (seller.getBalance() == null) {
                seller.setBalance(BigDecimal.ZERO);
            }
            seller.setBalance(seller.getBalance().add(order.getTotalPrice()));
            userMapper.updateByPrimaryKeySelective(seller);

            // 更新订单状态
            order.setStatus(2); // 待发货
            order.setPaymentTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateByPrimaryKeySelective(order);

            return R.createBySuccessMessage("支付成功，已扣除" + order.getTotalPrice() + "二手币");
        } catch (Exception e) {
            log.error("支付订单失败", e);
            return R.createByErrorMessage("支付失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> deliverOrder(Long orderId) {
        try {
            Long userId = HttpKit.getUserId();
            Order order = orderMapper.selectByPrimaryKey(orderId);

            if (order == null) {
                return R.createByErrorMessage("订单不存在");
            }

            if (!order.getSellerId().equals(userId)) {
                return R.createByErrorMessage("无权限操作");
            }

            if (order.getStatus() != 2) {
                return R.createByErrorMessage("订单状态不正确");
            }

            order.setStatus(3); // 待收货
            order.setDeliveryTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateByPrimaryKeySelective(order);

            return R.createBySuccessMessage("发货成功");
        } catch (Exception e) {
            log.error("发货失败", e);
            return R.createByErrorMessage("发货失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> confirmOrder(Long orderId) {
        try {
            Long userId = HttpKit.getUserId();
            Order order = orderMapper.selectByPrimaryKey(orderId);

            if (order == null) {
                return R.createByErrorMessage("订单不存在");
            }

            if (!order.getBuyerId().equals(userId)) {
                return R.createByErrorMessage("无权限操作");
            }

            if (order.getStatus() != 3) {
                return R.createByErrorMessage("订单状态不正确");
            }

            order.setStatus(4); // 已完成
            order.setFinishTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateByPrimaryKeySelective(order);

            // 将书籍状态设置为已售出
            Book book = bookMapper.selectByPrimaryKey(order.getBookId());
            if (book != null && book.getStock() == 0) {
                book.setStatus(2); // 已售出
                bookMapper.updateByPrimaryKeySelective(book);
            }

            return R.createBySuccessMessage("确认收货成功");
        } catch (Exception e) {
            log.error("确认收货失败", e);
            return R.createByErrorMessage("确认收货失败：" + e.getMessage());
        }
    }

    @Override
    public R<Order> findById(Long orderId) {
        try {
            Long userId = HttpKit.getUserId();
            Order order = orderMapper.selectByPrimaryKey(orderId);

            if (order == null) {
                return R.createByErrorMessage("订单不存在");
            }

            // 只有买家或卖家可以查看订单详情
            if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
                return R.createByErrorMessage("无权限查看");
            }

            return R.createBySuccess(order);
        } catch (Exception e) {
            log.error("查询订单详情失败", e);
            return R.createByErrorMessage("查询失败：" + e.getMessage());
        }
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = (int) ((Math.random() * 9 + 1) * 1000);
        return "ORD" + timestamp + random;
    }
}
