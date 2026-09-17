import { http } from '@/utils/request'

/** 当前用户信息（登录/me 返回） */
export interface UserInfo {
  id: number
  username: string
  realName?: string
  departmentId?: number
  roleId?: number
  roleCode?: string
  roleName?: string
  email?: string
  phone?: string
}

/** 登录入参 */
export interface LoginParams {
  username: string
  password: string
}

/** 登录出参 */
export interface LoginResult {
  token: string
  user: UserInfo
}

/** 登录：返回 JWT 与用户信息 */
export function login(params: LoginParams) {
  return http.post<LoginResult>('/auth/login', params)
}

/** 退出登录 */
export function logout() {
  return http.post<void>('/auth/logout')
}

/** 获取当前用户信息 */
export function getMe() {
  return http.get<UserInfo>('/auth/me')
}