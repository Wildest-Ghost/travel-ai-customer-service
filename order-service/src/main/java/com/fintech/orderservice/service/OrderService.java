package com.fintech.orderservice.service;

import com.fintech.orderservice.dto.CreateOrderRequest;
import com.fintech.orderservice.entity.Order;

import java.util.List;

public interface OrderService {

    Order findById(Long id);

    List<Order> findByUserId(Long userId);

    /** 创建订单：内部用 Feign 校验 user / product 存在，并从 product 拿 price */
    Order create(CreateOrderRequest req);

    /** 改签：原订单状态 → CHANGED；新建一条改签订单，original_order_id 指向原单 */
    Order changeOrder(Long originalOrderId, Long newProductId);
}
