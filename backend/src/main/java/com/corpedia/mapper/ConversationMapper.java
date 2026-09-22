package com.corpedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.corpedia.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话表 Mapper。继承 MyBatis-Plus BaseMapper，无自定义 SQL。
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}