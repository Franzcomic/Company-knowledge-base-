package com.corpedia.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corpedia.common.Result;
import com.corpedia.entity.Department;
import com.corpedia.mapper.DepartmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门（阶段4 P1）：列表接口供知识库/文档权限表单下拉（任意登录用户可读）。
 */
@Tag(name = "部门管理", description = "部门列表（任意登录用户可读）")
@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentMapper departmentMapper;

    public DepartmentController(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Operation(summary = "部门列表")
    @GetMapping
    public Result<List<Department>> list() {
        return Result.ok(departmentMapper.selectList(new QueryWrapper<Department>().orderByAsc("id")));
    }
}
