package com.corpedia.service;

import com.corpedia.common.BusinessException;
import com.corpedia.common.ResultCode;
import com.corpedia.dto.response.LoginResultVO;
import com.corpedia.dto.response.UserInfoVO;
import com.corpedia.entity.Role;
import com.corpedia.entity.User;
import com.corpedia.mapper.RoleMapper;
import com.corpedia.mapper.UserMapper;
import com.corpedia.security.JwtUtil;
import com.corpedia.security.UserContext;
import com.corpedia.security.UserContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务：登录校验并签发 JWT，/auth/me 返回当前用户信息。
 */
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, RoleMapper roleMapper,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /** 登录：校验用户名/密码与账号状态，成功后签发 JWT 并返回用户信息。 */
    public LoginResultVO login(String username, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号已被停用");
        }
        Role role = roleMapper.selectById(user.getRoleId());
        UserContext ctx = new UserContext(user.getId(), user.getUsername(),
                user.getDepartmentId(), user.getRoleId(), role == null ? null : role.getCode());
        String token = jwtUtil.generate(ctx);
        return new LoginResultVO(token, toVO(user, role));
    }

    /** 返回当前登录用户信息（依赖 JwtAuthFilter 注入的 UserContext）。 */
    public UserInfoVO me() {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        User user = userMapper.selectById(ctx.userId());
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
        }
        Role role = roleMapper.selectById(user.getRoleId());
        return toVO(user, role);
    }

    private UserInfoVO toVO(User user, Role role) {
        return new UserInfoVO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getDepartmentId(),
                user.getRoleId(),
                role == null ? null : role.getCode(),
                role == null ? null : role.getName(),
                user.getEmail(),
                user.getPhone()
        );
    }
}