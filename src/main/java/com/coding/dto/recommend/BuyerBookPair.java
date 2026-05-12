package com.coding.dto.recommend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 买家历史订单中的 (用户, 书籍) 二元组，用于协同过滤。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerBookPair {
    private Long buyerId;
    private Long bookId;
}
