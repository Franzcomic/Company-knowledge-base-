package com.corpedia.service;

import com.corpedia.common.BusinessException;
import com.corpedia.common.ResultCode;
import com.corpedia.entity.Role;
import com.corpedia.entity.User;
import com.corpedia.mapper.RoleMapper;
import com.corpedia.mapper.UserMapper;
import com.corpedia.security.UserContext;
import org.springframework.stereotype.Service;

/**
 * 阶段4 权限核心：
 * 1. buildVectorFilter(userId) —— RAG 检索前生成 Milvus 标量过滤表达式（部门 + 权限级别），
 *    SYS_ADMIN 返回 null = 全量可见；其余用户 strict: department_id in [0, 本部门] && permission_level <= 可读级别。
 * 2. userLevel(roleCode) —— 用户可读权限级别：SYS_ADMIN/DEPT_ADMIN=2，EMPLOYEE=1（文档权限 PUBLIC=0/DEPT=1/CONFIDENTIAL=2）。
 * 3. canAccess/requireRoles —— 端点级越权校验（已登录但无权限返回 403 FORBIDDEN，前端不跳登录）。
 */
@Service
public class PermissionService {

    public static final String ROLE_SYS_ADMIN = "SYS_ADMIN";
    public static final String ROLE_DEPT_ADMIN = "DEPT_ADMIN";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public PermissionService(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    /**
     * 用户可读权限级别（与 Milvus permission_level 整值对齐：PUBLIC=0/DEPT=1/CONFIDENTIAL=2）。
     * SYS_ADMIN / DEPT_ADMIN 可读部门范围内全部（含 CONFIDENTIAL）；EMPLOYEE 仅 PUBLIC + DEPT。
     */
    public int userLevel(String roleCode) {
        if (ROLE_SYS_ADMIN.equals(roleCode) || ROLE_DEPT_ADMIN.equals(roleCode)) {
            return 2;
        }
        return 1;
    }

    /**
     * 生成检索过滤表达式（裸键 DSL，Spring AI 转换器自动包装为 metadata["..."]）：
     * <pre>department_id in [0, &lt;deptId&gt;] && permission_level &lt;= &lt;level&gt;</pre>
     * 返回 null 表示不加过滤（SYS_ADMIN 全量可见）。
     * 用户不存在 / 角色缺失时保守收敛为「仅全司 PUBLIC」，避免越权放大。
     */
    public String buildVectorFilter(Long userId) {
        User user = userId == null ? null : userMapper.selectById(userId);
        if (user == null) {
            return "department_id in [0, 0] && permission_level <= 0";
        }
        Role role = user.getRoleId() == null ? null : roleMapper.selectById(user.getRoleId());
        String roleCode = role == null ? null : role.getCode();
        if (ROLE_SYS_ADMIN.equals(roleCode)) {
            return null;   // 系统管理员全量可见
        }
        int dept = user.getDepartmentId() == null ? 0 : user.getDepartmentId().intValue();
        int level = userLevel(roleCode);
        return "department_id in [0, " + dept + "] && permission_level <= " + level;
    }

    /** 资源级越权校验（resource -> 允许的角色集合）。未知资源一律拒绝。 */
    public boolean canAccess(UserContext ctx, String resource) {
        if (ctx == null || ctx.roleCode() == null) {
            return false;
        }
        if (ROLE_SYS_ADMIN.equals(ctx.roleCode())) {
            return true;
        }
        return switch (resource) {
            case "user:manage" -> false;                                // 仅 SYS_ADMIN
            case "doc:manage" -> ROLE_DEPT_ADMIN.equals(ctx.roleCode()); // 文档权限/重向量化
            default -> false;
        };
    }

    /** 无权限访问时抛出 403（区别于未登录 401，前端不会跳登录）。 */
    public BusinessException forbidden() {
        return new BusinessException(ResultCode.FORBIDDEN, "无权访问");
    }

    /** 便捷：SYS_ADMIN 才能访问用户管理。 */
    public void requireUserManage(UserContext ctx) {
        if (!canAccess(ctx, "user:manage")) {
            throw forbidden();
        }
    }

    /** 便捷：文档权限/重向量化需要 SYS_ADMIN 或 DEPT_ADMIN。 */
    public void requireDocManage(UserContext ctx) {
        if (!canAccess(ctx, "doc:manage")) {
            throw forbidden();
        }
    }
}
