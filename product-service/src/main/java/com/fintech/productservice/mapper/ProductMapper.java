package com.fintech.productservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fintech.productservice.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
