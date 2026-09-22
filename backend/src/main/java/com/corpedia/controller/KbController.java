package com.corpedia.controller;

import com.corpedia.common.Result;
import com.corpedia.dto.request.KbCreateRequest;
import com.corpedia.dto.request.KbUpdateRequest;
import com.corpedia.dto.response.KbVO;
import com.corpedia.security.UserContext;
import com.corpedia.security.UserContextHolder;
import com.corpedia.service.KbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 知识库管理控制器：知识库 CRUD。
 * 列表按当前用户可见范围过滤；删除会级联清理该库下文档、磁盘文件与 Milvus 向量。
 */
@Tag(name = "知识库管理", description = "知识库 CRUD；删除会级联清理该库下文档、磁盘文件与 Milvus 向量")
@RestController
@RequestMapping("/kb")
public class KbController {

    private final KbService kbService;

    public KbController(KbService kbService) {
        this.kbService = kbService;
    }

    @Operation(summary = "知识库列表", description = "按当前用户可见范围返回知识库列表")
    @GetMapping
    public Result<List<KbVO>> list() {
        return Result.ok(kbService.list());
    }

    @Operation(summary = "创建知识库", description = "permissionLevel: PUBLIC/DEPT/CONFIDENTIAL；departmentId 为空表示全司")
    @PostMapping
    public Result<KbVO> create(@Valid @RequestBody KbCreateRequest req) {
        UserContext ctx = UserContextHolder.get();
        return Result.ok(kbService.create(req, ctx == null ? null : ctx.userId()));
    }

    @Operation(summary = "更新知识库", description = "可更新名称/描述/部门/权限级别")
    @PutMapping("/{id}")
    public Result<KbVO> update(@Parameter(description = "知识库 id") @PathVariable Long id,
                               @RequestBody KbUpdateRequest req) {
        return Result.ok(kbService.update(id, req));
    }

    @Operation(summary = "删除知识库", description = "级联删除该库下文档、磁盘文件与 Milvus 向量")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "知识库 id") @PathVariable Long id) {
        kbService.delete(id);
        return Result.ok();
    }
}
