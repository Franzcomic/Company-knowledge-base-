package com.corpedia.dto.response;

/**
 * 部门分布：{ deptName, count }。
 */
public record DepartmentStatVO(
        /** 部门名称（未分配则为"未分配"）。 */
        String deptName,
        /** 问题数。 */
        long count
) {
}