package com.corpedia.security;

/**
 * UserContext 的 ThreadLocal 容器：请求进入时 set，结束（JwtAuthFilter finally）时 clear，避免线程复用串数据。
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext ctx) {
        HOLDER.set(ctx);
    }

    public static UserContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}