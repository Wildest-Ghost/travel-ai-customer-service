package com.fintech.productservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fintech.productservice.entity.Product;
import com.fintech.productservice.mapper.ProductMapper;
import com.fintech.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Product findById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<Product> findAll() {
        return productMapper.selectList(null);
    }

    @Override
    public List<Product> findByType(String type) {
        return productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getType, type)
        );
    }
}
