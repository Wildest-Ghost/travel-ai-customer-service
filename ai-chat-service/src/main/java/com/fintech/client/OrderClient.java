package com.fintech.client;

import com.fintech.client.dto.OrderVO;
import com.fintech.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/orders/{id}")
    Result<OrderVO> findById(@PathVariable("id") Long id);

    @GetMapping("/orders")
    Result<List<OrderVO>> findByUserId(@RequestParam("userId") Long userId);

    @PostMapping("/orders/{id}/change")
    Result<OrderVO> changeOrder(@PathVariable("id") Long originalOrderId,
                                @RequestParam("newProductId") Long newProductId);
}
