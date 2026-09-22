package com.corpedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.corpedia.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价表 Mapper。继承 MyBatis-Plus BaseMapper，无自定义 SQL。
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}
