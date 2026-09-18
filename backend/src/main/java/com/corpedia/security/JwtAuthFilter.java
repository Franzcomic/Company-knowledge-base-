package com.corpedia.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import com.corpedia.entity.User;
import com.corpedia.entity.Role;
import com.corpedia.mapper.UserMapper;
import com.corpedia.mapper.RoleMapper;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper users;
    private final RoleMapper roles;

    public JwtAuthFilter(JwtUtil jwtUtil, UserMapper users, RoleMapper roles) {
        this.jwtUtil = jwtUtil;
        this.users = users;
        this.roles = roles;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                UserContext ctx = jwtUtil.parse(token);
                User user = users.selectById(ctx.userId());
                if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
                    throw new IllegalArgumentException("Inactive account");
                }
                Role role = user.getRoleId() == null ? null : roles.selectById(user.getRoleId());
                if (role == null) throw new IllegalArgumentException("Missing role");
                ctx = new UserContext(user.getId(), user.getUsername(), user.getDepartmentId(),
                        user.getRoleId(), role.getCode());
                UserContextHolder.set(ctx);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ctx.username(), null, List.of());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                UserContextHolder.clear();
                SecurityContextHolder.clearContext();
                // token 无效则不注入身份, 由 SecurityConfig 兜底
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }
}
