package com.corpedia.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corpedia.entity.Department;
import com.corpedia.entity.Role;
import com.corpedia.entity.User;
import com.corpedia.mapper.DepartmentMapper;
import com.corpedia.mapper.RoleMapper;
import com.corpedia.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * 幂等种子数据：角色 3、部门 3、用户 4（管理员/部门管理员/员工2）。
 * 仅显式开启 SEED_ENABLED 后创建演示账号，密码由 SEED_PASSWORD 注入并 BCrypt 哈希。
 */
@Component
@ConditionalOnProperty(name = "corpedia.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleMapper roleMapper;
    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    @Value("${corpedia.seed.password}")
    private String seedPassword;

    public DataSeeder(RoleMapper roleMapper, DepartmentMapper departmentMapper,
                      UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.roleMapper = roleMapper;
        this.departmentMapper = departmentMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (seedPassword == null || seedPassword.length() < 12) {
            throw new IllegalStateException("SEED_PASSWORD must contain at least 12 characters");
        }
        // 表结构由 spring.sql.init 依据 db/schema.sql 自动创建
        seedRoles();
        seedDepartments();
        seedUsers();
        log.info("[DataSeeder] 种子数据就绪");
    }

    private void seedRoles() {
        if (roleMapper.selectCount(new QueryWrapper<Role>()).intValue() > 0) return;
        insertRole("SYS_ADMIN", "系统管理员");
        insertRole("DEPT_ADMIN", "部门管理员");
        insertRole("EMPLOYEE", "员工");
        log.info("[DataSeeder] roles seeded");
    }

    private void insertRole(String code, String name) {
        if (roleMapper.selectCount(new QueryWrapper<Role>().eq("code", code)).intValue() > 0) return;
        Role r = new Role();
        r.setCode(code);
        r.setName(name);
        roleMapper.insert(r);
    }

    private void seedDepartments() {
        if (departmentMapper.selectCount(new QueryWrapper<Department>()).intValue() > 0) return;
        insertDept("财务部");
        insertDept("研发部");
        insertDept("全司");
        log.info("[DataSeeder] departments seeded");
    }

    private void insertDept(String name) {
        if (departmentMapper.selectCount(new QueryWrapper<Department>().eq("name", name)).intValue() > 0) return;
        Department d = new Department();
        d.setName(name);
        d.setParentId(null);
        departmentMapper.insert(d);
    }

    private void seedUsers() {
        if (userMapper.selectCount(new QueryWrapper<User>()).intValue() > 0) return;
        Long sysAdminRole = idByRole("SYS_ADMIN");
        Long deptAdminRole = idByRole("DEPT_ADMIN");
        Long empRole = idByRole("EMPLOYEE");
        Long finance = idByDept("财务部");
        Long rd = idByDept("研发部");
        Long corp = idByDept("全司");

        insertUser("admin", "系统管理员", sysAdminRole, corp, "admin@corpedia.com", "13800000000");
        insertUser("zhangsan", "张三", deptAdminRole, finance, "zhangsan@corpedia.com", "13800000001");
        insertUser("lisi", "李四", empRole, finance, "lisi@corpedia.com", "13800000002");
        insertUser("wangwu", "王五", empRole, rd, "wangwu@corpedia.com", "13800000003");
        log.info("[DataSeeder] users seeded");
    }

    private void insertUser(String username, String realName, Long roleId, Long deptId,
                            String email, String phone) {
        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", username)).intValue() > 0) return;
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(seedPassword));
        u.setRealName(realName);
        u.setRoleId(roleId);
        u.setDepartmentId(deptId);
        u.setEmail(email);
        u.setPhone(phone);
        u.setStatus(1);
        userMapper.insert(u);
    }

    private Long idByRole(String code) {
        Role r = roleMapper.selectOne(new QueryWrapper<Role>().eq("code", code));
        return r == null ? null : r.getId();
    }

    private Long idByDept(String name) {
        Department d = departmentMapper.selectOne(new QueryWrapper<Department>().eq("name", name));
        return d == null ? null : d.getId();
    }
}
