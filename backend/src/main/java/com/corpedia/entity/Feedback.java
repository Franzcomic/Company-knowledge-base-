package com.corpedia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回答评价（表 feedback）：对 assistant 消息点赞/点踩 + 可选原因。
 */
@Data
@TableName("feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long messageId;
    private Long userId;
    private String rating;              // UP / DOWN
    private String reason;
    private LocalDateTime createdAt;
}
