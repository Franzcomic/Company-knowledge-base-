package com.corpedia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库实体，对应表 knowledge_base。
 * departmentId 为空表示全司共享；permissionLevel 控制访问范围（PUBLIC/DEPT/CONFIDENTIAL）。
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBase {

    /** 主键，自增。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 知识库名称。 */
    private String name;

    /** 所属部门 id，可空=全公司。 */
    private Long departmentId;          // 可空=全公司

    /** 知识库描述。 */
    private String description;

    /** 权限级别：PUBLIC / DEPT / CONFIDENTIAL。 */
    private String permissionLevel;     // PUBLIC / DEPT / CONFIDENTIAL

    /** 创建人用户 id。 */
    private Long createdBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
