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

    /** 主键，自增。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被评价的 assistant 消息 id。 */
    private Long messageId;

    /** 评价人用户 id。 */
    private Long userId;

    /** 评价类型：UP / DOWN。 */
    private String rating;              // UP / DOWN

    /** 可选评价原因。 */
    private String reason;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
