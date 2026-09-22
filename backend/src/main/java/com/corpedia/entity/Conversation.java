package com.corpedia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话（表 conversation）。某用户的一次对话线程。
 */
@Data
@TableName("conversation")
public class Conversation {

    /** 主键，自增。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话归属用户 id。 */
    private Long userId;

    /** 会话标题，首条消息自动截取填充。 */
    private String title;

    /** 冗余用户当前部门（阶段4 权限用）。 */
    private Long departmentId;          // 冗余用户当前部门（阶段4 权限用）

    /** 状态：1 正常 / 0 关闭。 */
    private Integer status;             // 1 正常 / 0 关闭

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}