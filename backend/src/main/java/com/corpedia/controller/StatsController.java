package com.corpedia.controller;

import com.corpedia.common.Result;
import com.corpedia.dto.response.DepartmentStatVO;
import com.corpedia.dto.response.HotKbVO;
import com.corpedia.dto.response.HotQuestionVO;
import com.corpedia.dto.response.StatsOverviewVO;
import com.corpedia.security.UserContext;
import com.corpedia.security.UserContextHolder;
import com.corpedia.service.PermissionService;
import com.corpedia.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计看板（阶段5 P2，仅 SYS_ADMIN）。
 */
@Tag(name = "统计看板", description = "趋势/热门问题/热门知识库/部门分布/命中率/响应时间/满意率（仅系统管理员）")
@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;
    private final PermissionService permissionService;

    public StatsController(StatsService statsService, PermissionService permissionService) {
        this.statsService = statsService;
        this.permissionService = permissionService;
    }

    @Operation(summary = "统计总览", description = "问题量/命中率/响应时间/满意率/逐日趋势")
    @GetMapping("/overview")
    public Result<StatsOverviewVO> overview(
            @Parameter(description = "时间范围，如 7d/30d/90d/all") @RequestParam(defaultValue = "7d") String range) {
        requireAdmin();
        return Result.ok(statsService.overview(range));
    }

    @Operation(summary = "热门问题", description = "按问题原文分组计数，Top N")
    @GetMapping("/hot-questions")
    public Result<List<HotQuestionVO>> hotQuestions(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "7d") String range,
            @Parameter(description = "返回条数") @RequestParam(defaultValue = "10") int limit) {
        requireAdmin();
        return Result.ok(statsService.hotQuestions(range, limit));
    }

    @Operation(summary = "热门知识库", description = "按来源被引次数统计")
    @GetMapping("/hot-kb")
    public Result<List<HotKbVO>> hotKb(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "7d") String range,
            @Parameter(description = "返回条数") @RequestParam(defaultValue = "10") int limit) {
        requireAdmin();
        return Result.ok(statsService.hotKb(range, limit));
    }

    @Operation(summary = "部门分布", description = "按会话发起部门统计问题数")
    @GetMapping("/departments")
    public Result<List<DepartmentStatVO>> departments(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "7d") String range) {
        requireAdmin();
        return Result.ok(statsService.departments(range));
    }

    @Operation(summary = "未解决问题", description = "被拒答（answered=0）的问题原文")
    @GetMapping("/unsolved")
    public Result<List<String>> unsolved(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "7d") String range,
            @Parameter(description = "返回条数") @RequestParam(defaultValue = "10") int limit) {
        requireAdmin();
        return Result.ok(statsService.unsolved(range, limit));
    }

    private void requireAdmin() {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null) {
            throw permissionService.forbidden();
        }
        permissionService.requireUserManage(ctx);
    }
}