package com.fintech.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class SentinelChatRules {
    @PostConstruct
    public void init() {
        DegradeRule rule = new DegradeRule("aiChat");
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO); // 按异常比例熔断
        rule.setCount(0.5);            // 异常率 > 50%
        rule.setMinRequestAmount(5);   // 至少 5 个请求才开始统计
        rule.setStatIntervalMs(10000); // 统计窗口 10s
        rule.setTimeWindow(10);        // 熔断 10s，之后半开试探
        DegradeRuleManager.loadRules(Collections.singletonList(rule));
    }
}