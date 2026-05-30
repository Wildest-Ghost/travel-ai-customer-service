package com.fintech.history.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fintech.history.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
