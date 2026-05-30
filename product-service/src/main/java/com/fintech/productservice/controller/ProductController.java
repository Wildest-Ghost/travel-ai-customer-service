package com.fintech.productservice.controller;

import com.fintech.common.Result;
import com.fintech.productservice.entity.Product;
import com.fintech.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public Result<Product> findById(@PathVariable Long id) {
        return Result.success(productService.findById(id));
    }

    /** type 可选；不传则查全部 */
    @GetMapping
    public Result<List<Product>> list(@RequestParam(required = false) String type) {
        return Result.success(
                type == null ? productService.findAll() : productService.findByType(type)
        );
    }
}
