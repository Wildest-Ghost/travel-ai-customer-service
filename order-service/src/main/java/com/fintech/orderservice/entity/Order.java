package com.fintech.orderservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单。
 *
 * 跨服务关联：user_id / product_id 仅为普通 BIGINT 列，不设 DB 外键，
 * 由应用层（创建订单时 Feign 调用对应服务校验）保证一致性。
 *
 * 改签建模：原订单 status 置为 CHANGED，另建一条改签订单，
 * 通过 original_order_id 关联原订单，保留可审计的变更链。
 */
@Data
@TableName("sys_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务订单号 */
    private String orderNo;

    /** 关联用户 id（应用层关联 sys_user.id，DB 层无 FK） */
    private Long userId;

    /** 关联产品 id（应用层关联 sys_product.id，DB 层无 FK） */
    private Long productId;

    /** PENDING / PAID / CANCELLED / CHANGED / REFUNDED */
    private String status;

    private BigDecimal amount;
    private Integer quantity;

    /** 出行开始时间（起飞 / 入住） */
    private LocalDateTime startTime;
    /** 退房时间（仅酒店） */
    private LocalDateTime endTime;

    private String contactName;
    private String contactPhone;

    /** 改签订单指向原订单 id（仅改签订单有值） */
    private Long originalOrderId;
    /** 'CHANGE' / 'REFUND' / null */
    private String changeType;
}
