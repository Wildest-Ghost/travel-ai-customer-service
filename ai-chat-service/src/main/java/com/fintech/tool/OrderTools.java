package com.fintech.tool;

import com.fintech.client.OrderClient;
import com.fintech.client.dto.OrderVO;
import com.fintech.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 给 LLM 调用的工具集合。
 *
 * 关键：@Tool 的 description 是 LLM【唯一】用来判断"该不该调这个工具"的依据，
 *      所以写得越具体越准，含糊词（"查询订单"）会让 LLM 摸不到头脑。
 */
@Component
@RequiredArgsConstructor
public class OrderTools {

    private final OrderClient orderClient;

    @Tool(description = """
            按订单的数字 ID 查询单个订单的详情。
            返回字段：订单号、用户 ID、产品 ID、订单状态(PENDING/PAID/CANCELLED/CHANGED/REFUNDED)、金额、数量、出行开始时间、出行结束时间。
            使用时机：用户明确给出订单号询问订单状态、出行时间、金额等场景。
            如果订单不存在，会抛异常并附带"订单 X 不存在"的信息。
            """)
    public OrderVO findOrderById(
            @ToolParam(description = "订单的数字 ID（Long 类型整数）。用户口语中'订单 5'、'我的订单号 1001'里的数字部分就是这个值。")
            Long orderId) {
        Result<OrderVO> res = orderClient.findById(orderId);
        OrderVO order = res != null ? res.getData() : null;
        if (order == null) {
            // Spring AI 会把异常 message 作为工具结果回给 LLM，LLM 据此告知用户找不到
            throw new IllegalArgumentException("订单 " + orderId + " 不存在");
        }
        return order;
    }

    @Tool(description = """
            列出指定用户名下的所有订单。
            返回订单列表，每条订单包含状态、产品 ID、出行时间、金额、数量等。
            使用时机：用户询问"我的订单有哪些"、"XX 用户的所有订单"等场景。
            返回空列表表示该用户暂无订单。
            """)
    public List<OrderVO> findOrdersByUserId(
            @ToolParam(description = "用户的数字 ID（Long 类型整数）。已登录用户的身份 ID，或客服需要查询的目标用户 ID。")
            Long userId) {
        Result<List<OrderVO>> res = orderClient.findByUserId(userId);
        return res != null && res.getData() != null ? res.getData() : List.of();
    }
}
