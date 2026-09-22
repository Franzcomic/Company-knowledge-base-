package com.corpedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.corpedia.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 Mapper。继承 MyBatis-Plus BaseMapper，无自定义 SQL。
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}