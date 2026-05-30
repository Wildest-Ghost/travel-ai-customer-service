package com.fintech.productservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品（机票 + 酒店）。
 * 用一张表 + type 字段区分机票/酒店，简化模型。
 */
@Data
@TableName("sys_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** FLIGHT / HOTEL */
    private String type;

    /** 航班号 / 酒店名 */
    private String name;

    /** 出发地（FLIGHT）/ 酒店所在城市（HOTEL） */
    private String origin;

    /** 目的地（仅 FLIGHT） */
    private String destination;

    private BigDecimal price;
    private Integer stock;

    /** 起飞时间 / 入住日期 */
    private LocalDateTime startTime;
    /** 到达时间 / 退房日期 */
    private LocalDateTime endTime;

    private String description;
}
