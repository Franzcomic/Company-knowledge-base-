package com.corpedia.security;

public record UserContext(Long userId, String username, Long deptId, Long roleId, String roleCode) {
}