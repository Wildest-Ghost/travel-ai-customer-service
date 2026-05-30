package com.fintech.history.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fintech.history.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
