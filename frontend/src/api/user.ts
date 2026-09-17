import { http } from '@/utils/request'

/** 用户管理列表项（GET /api/users，管理员视图，较 UserInfo 多 status/roleId） */
export interface UserItem {
  id: number
  username: string
  realName?: string
  departmentId?: number | null
  roleId?: number
  roleCode?: string
  roleName?: string
  status: number // 1 正常 / 0 停用
  email?: string
  phone?: string
}

/** 角色列表项（GET /api/roles） */
export interface Role {
  id: number
  code: string
  name: string
}

/** 更新用户入参（PUT /api/users/{id}：departmentId/roleId/status 均可选） */
export interface UserUpdateParams {
  departmentId?: number | null
  roleId?: number
  status?: number
}

/** 用户列表（仅 SYS_ADMIN，403 拦截） */
export function listUsers() {
  return http.get<UserItem[]>('/users')
}

/** 更新用户：分配部门 / 角色 / 启停用（仅 SYS_ADMIN） */
export function updateUser(id: number, data: UserUpdateParams) {
  return http.put<void>(`/users/${id}`, data)
}

/** 角色列表（任意登录用户可读，供用户管理下拉） */
export function listRoles() {
  return http.get<Role[]>('/roles')
}
