package com.corpedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.corpedia.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息表 Mapper。继承 MyBatis-Plus BaseMapper，无自定义 SQL。
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}