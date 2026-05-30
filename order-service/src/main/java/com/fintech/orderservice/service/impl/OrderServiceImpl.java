package com.fintech.orderservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fintech.common.BusinessException;
import com.fintech.orderservice.client.ProductClient;
import com.fintech.orderservice.client.UserClient;
import com.fintech.orderservice.client.dto.ProductVO;
import com.fintech.orderservice.client.dto.UserVO;
import com.fintech.orderservice.dto.CreateOrderRequest;
import com.fintech.orderservice.entity.Order;
import com.fintech.orderservice.mapper.OrderMapper;
import com.fintech.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final UserClient userClient;
    private final ProductClient productClient;

    @Override
    public Order findById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId)
        );
    }

    @Override
    @Transactional
    public Order create(CreateOrderRequest req) {
        // 1. Feign 校验用户存在
        UserVO user = userClient.findById(req.getUserId()).getData();
        if (user == null) {
            throw new BusinessException("用户不存在: " + req.getUserId());
        }
        // 2. Feign 校验产品存在 + 拿 price / 时间
        ProductVO product = productClient.findById(req.getProductId()).getData();
        if (product == null) {
            throw new BusinessException("产品不存在: " + req.getProductId());
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(req.getUserId());
        order.setProductId(req.getProductId());
        order.setQuantity(req.getQuantity());
        order.setAmount(product.getPrice().multiply(BigDecimal.valueOf(req.getQuantity())));
        order.setStatus("PENDING");
        order.setContactName(req.getContactName());
        order.setContactPhone(req.getContactPhone());
        order.setStartTime(product.getStartTime());
        order.setEndTime(product.getEndTime());
        orderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional
    public Order changeOrder(Long originalOrderId, Long newProductId) {
        Order original = orderMapper.selectById(originalOrderId);
        if (original == null) {
            throw new BusinessException("原订单不存在: " + originalOrderId);
        }
        if (!"PAID".equals(original.getStatus())) {
            throw new BusinessException("只有已支付订单可以改签，当前状态: " + original.getStatus());
        }

        ProductVO newProduct = productClient.findById(newProductId).getData();
        if (newProduct == null) {
            throw new BusinessException("新产品不存在: " + newProductId);
        }

        // 新建改签订单（指向原订单）
        Order changeOrder = new Order();
        changeOrder.setOrderNo(generateOrderNo());
        changeOrder.setUserId(original.getUserId());
        changeOrder.setProductId(newProductId);
        changeOrder.setQuantity(original.getQuantity());
        changeOrder.setAmount(newProduct.getPrice().multiply(BigDecimal.valueOf(original.getQuantity())));
        changeOrder.setStatus("PENDING");
        changeOrder.setContactName(original.getContactName());
        changeOrder.setContactPhone(original.getContactPhone());
        changeOrder.setStartTime(newProduct.getStartTime());
        changeOrder.setEndTime(newProduct.getEndTime());
        changeOrder.setOriginalOrderId(originalOrderId);
        changeOrder.setChangeType("CHANGE");
        orderMapper.insert(changeOrder);

        // 原订单 → CHANGED（不动其他字段，审计可追溯）
        original.setStatus("CHANGED");
        orderMapper.updateById(original);

        return changeOrder;
    }

    private String generateOrderNo() {
        return "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
