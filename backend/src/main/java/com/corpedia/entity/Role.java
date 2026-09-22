package com.corpedia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体，对应表 role。
 * code 用于权限判定：SYS_ADMIN / DEPT_ADMIN / EMPLOYEE。
 */
@Data
@TableName("role")
public class Role {

    /** 主键，自增。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码，权限判定的唯一依据。 */
    private String code;

    /** 角色展示名。 */
    private String name;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}