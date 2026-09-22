package com.corpedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.corpedia.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper。继承 MyBatis-Plus BaseMapper 获得基础 CRUD，无自定义 SQL。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}