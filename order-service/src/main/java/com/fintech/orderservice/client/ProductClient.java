package com.fintech.orderservice.client;

import com.fintech.common.Result;
import com.fintech.orderservice.client.dto.ProductVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    Result<ProductVO> findById(@PathVariable("id") Long id);
}
