package com.corpedia.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corpedia.common.BusinessException;
import com.corpedia.common.ResultCode;
import com.corpedia.dto.request.UserUpdateRequest;
import com.corpedia.dto.response.UserVO;
import com.corpedia.entity.Role;
import com.corpedia.entity.User;
import com.corpedia.mapper.RoleMapper;
import com.corpedia.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户管理（阶段4，仅 SYS_ADMIN 可调）：用户列表 + 改部门/角色/启停用。
 */
@Service
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public UserService(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    public List<UserVO> list() {
        return userMapper.selectList(new QueryWrapper<User>().orderByAsc("id"))
                .stream().map(this::toVO).toList();
    }

    public void update(Long id, UserUpdateRequest req) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (req.roleId() != null) {
            Role role = roleMapper.selectById(req.roleId());
            if (role == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "角色不存在");
            }
            user.setRoleId(req.roleId());
        }
        if (req.departmentId() != null) {
            user.setDepartmentId(req.departmentId());
        }
        if (req.status() != null) {
            if (req.status() != 0 && req.status() != 1) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "status 仅支持 0(停用)/1(正常)");
            }
            user.setStatus(req.status());
        }
        userMapper.updateById(user);
    }

    private UserVO toVO(User u) {
        Role role = u.getRoleId() == null ? null : roleMapper.selectById(u.getRoleId());
        return new UserVO(u.getId(), u.getUsername(), u.getRealName(), u.getDepartmentId(),
                u.getRoleId(), role == null ? null : role.getCode(), role == null ? null : role.getName(),
                u.getStatus(), u.getEmail(), u.getPhone());
    }
}
