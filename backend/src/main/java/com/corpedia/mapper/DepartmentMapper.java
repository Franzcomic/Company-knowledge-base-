package com.corpedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.corpedia.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门表 Mapper。继承 MyBatis-Plus BaseMapper，无自定义 SQL。
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}