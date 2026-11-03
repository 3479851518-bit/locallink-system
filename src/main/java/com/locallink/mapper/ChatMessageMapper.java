package com.locallink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.locallink.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
}
