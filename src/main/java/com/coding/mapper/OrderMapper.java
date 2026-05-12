package com.coding.mapper;

import com.coding.dto.recommend.BuyerBookPair;
import com.coding.entity.Order;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 订单Mapper接口
 *
 * @author system
 * @since 2025-01-24
 */
public interface OrderMapper extends Mapper<Order> {

    /**
     * 分页查询订单列表（带关联查询）
     */
    List<Order> selectOrderListWithDetails(@Param("buyerId") Long buyerId,
                                           @Param("sellerId") Long sellerId,
                                           @Param("status") Integer status);

    /**
     * 已支付及之后状态的订单（用于协同过滤：用户买过的书）
     */
    List<BuyerBookPair> listPurchasedBuyerBookPairs();

    /**
     * 某买家已购书籍 ID（去重）
     */
    List<Long> listBookIdsPurchasedByBuyer(@Param("buyerId") Long buyerId);
}
