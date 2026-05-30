package com.fintech.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一把异常转换成 Result JSON 返回。
 *
 * 该类位于共享模块 common，需业务服务的启动类扫描到 com.fintech.common 包才生效，
 * 故各服务启动类统一声明 @SpringBootApplication(scanBasePackages = "com.fintech")。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：用 BusinessException 自带的 code 和 message */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 兜底：未预期异常。日志记完整堆栈，但只给前端返回脱敏后的提示 */
    @ExceptionHandler(Exception.class)
    public Result<?> handleOther(Exception e) {
        log.error("未预期异常", e);
        return Result.error(500, "系统错误，请稍后重试");
    }
}
