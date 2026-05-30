package com.fintech.productservice.service;

import com.fintech.productservice.entity.Product;

import java.util.List;

public interface ProductService {
    Product findById(Long id);
    List<Product> findAll();
    List<Product> findByType(String type);
}
