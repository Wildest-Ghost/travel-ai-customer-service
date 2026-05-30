package com.fintech.tool;

import com.fintech.client.OrderClient;
import com.fintech.client.dto.OrderVO;
import com.fintech.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * ChangeAgent 用的工具：发起改签。
 *
 * 行为：
 *   1) 调 order-service 的改签接口（POST /orders/{id}/change）
 *   2) order-service 内部会校验原订单状态（必须 PAID），再新建一条改签订单
 *   3) 这里只负责"代为发起"，业务规则在 order-service 那边把关
 *
 * 失败处理：
 *   - 原订单不存在 / 状态不对 / 新产品不存在 → 抛 IllegalArgumentException
 *   - Spring AI 会把异常 message 喂回给 LLM，LLM 据此告知用户"改签失败原因"
 */
@Component
@RequiredArgsConstructor
public class ChangeTools {

    private final OrderClient orderClient;

    @Tool(description = """
            发起订单改签：基于原订单 ID 和新产品 ID，创建一条改签订单，并把原订单状态变更为 CHANGED。
            返回新建的改签订单详情（订单号、新出行时间、新金额、状态 PENDING）。
            使用时机：用户【明确】表达改签意愿，并且【同时给出】原订单号和目标产品（航班/酒店）ID。
            禁止使用：用户没说改签 / 没明确指出新产品时；这种情况应该回去问清楚，不要瞎调。
            失败语义：会抛异常并附带原因，比如"原订单不存在"、"原订单未支付不能改签"、"新产品不存在"。
            """)
    public OrderVO changeOrder(
            @ToolParam(description = "原订单的数字 ID（Long）。用户口语中'我要改签订单 1'里的 1 就是这个值。")
            Long originalOrderId,
            @ToolParam(description = "用户想改到的新产品（航班/酒店）数字 ID（Long）。如果用户没给，不要瞎调本工具，而是先问用户。")
            Long newProductId
    ) {
        Result<OrderVO> res = orderClient.changeOrder(originalOrderId, newProductId);
        OrderVO newOrder = res != null ? res.getData() : null;
        if (newOrder == null) {
            String reason = (res != null && res.getMsg()!= null) ? res.getMsg() : "未知原因";
            // 兜底：order-service 返回 null 时（理论上业务异常已被 GlobalExceptionHandler 包装，但 Feign 反序列化可能丢失）
            throw new IllegalArgumentException(
                "改签失败:" +reason
            );
        }
        return newOrder;
    }
}
