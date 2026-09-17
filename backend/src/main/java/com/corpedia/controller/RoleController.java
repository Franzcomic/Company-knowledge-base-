package com.corpedia.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corpedia.common.Result;
import com.corpedia.dto.response.RoleVO;
import com.corpedia.entity.Role;
import com.corpedia.mapper.RoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色（阶段4 P1）：列表接口供用户管理页下拉（任意登录用户可读）。
 */
@Tag(name = "角色管理", description = "角色列表（任意登录用户可读）")
@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleMapper roleMapper;

    public RoleController(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Operation(summary = "角色列表")
    @GetMapping
    public Result<List<RoleVO>> list() {
        return Result.ok(roleMapper.selectList(new QueryWrapper<Role>().orderByAsc("id"))
                .stream().map(r -> new RoleVO(r.getId(), r.getCode(), r.getName())).toList());
    }
}
