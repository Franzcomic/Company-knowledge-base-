package com.corpedia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_base")
public class KnowledgeBase {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long departmentId;          // 可空=全公司
    private String description;
    private String permissionLevel;     // PUBLIC / DEPT / CONFIDENTIAL
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
