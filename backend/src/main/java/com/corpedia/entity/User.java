package com.corpedia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应表 user。
 * 存储登录账号、BCrypt 密码哈希、所属部门/角色与启停用状态。
 */
@Data
@TableName("user")
public class User {

    /** 主键，自增。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录名，唯一。 */
    private String username;

    /** BCrypt 加密后的密码，不存明文。 */
    private String passwordHash;

    /** 真实姓名。 */
    private String realName;

    /** 邮箱。 */
    private String email;

    /** 手机号。 */
    private String phone;

    /** 所属部门 id（可空）。 */
    private Long departmentId;

    /** 角色 id。 */
    private Long roleId;

    /** 账号状态：1 正常 / 0 停用。 */
    private Integer status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}