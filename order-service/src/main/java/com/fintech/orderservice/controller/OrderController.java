package com.fintech.orderservice.controller;

import com.fintech.common.Result;
import com.fintech.orderservice.dto.CreateOrderRequest;
import com.fintech.orderservice.entity.Order;
import com.fintech.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public Result<Order> findById(@PathVariable Long id) {
        return Result.success(orderService.findById(id));
    }

    /** 按用户查订单：AI 客服会高频用 */
    @GetMapping
    public Result<List<Order>> findByUserId(@RequestParam Long userId) {
        return Result.success(orderService.findByUserId(userId));
    }

    /** 创建订单：内部 Feign 校验 user/product */
    @PostMapping
    public Result<Order> create(@Valid @RequestBody CreateOrderRequest req) {
        return Result.success(orderService.create(req));
    }

    /** 改签：路径里的 id 是原订单 id，新产品 id 走 query 参数 */
    @PostMapping("/{id}/change")
    public Result<Order> change(@PathVariable("id") Long originalOrderId,
                                @RequestParam Long newProductId) {
        return Result.success(orderService.changeOrder(originalOrderId, newProductId));
    }
}
