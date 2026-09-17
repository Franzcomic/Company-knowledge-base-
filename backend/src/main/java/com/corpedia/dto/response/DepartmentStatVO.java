package com.corpedia.dto.response;

/**
 * 部门分布：{ deptName, count }。
 */
public record DepartmentStatVO(String deptName, long count) {
}