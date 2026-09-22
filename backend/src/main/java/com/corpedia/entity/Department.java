package com.corpedia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门实体，对应表 department。
 * parentId 预留层级，当前种子数据为扁平结构。
 */
@Data
@TableName("department")
public class Department {

    /** 主键，自增。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 部门名称。 */
    private String name;

    /** 父部门 id（预留，当前未使用）。 */
    private Long parentId;

    /** 部门描述。 */
    private String description;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}