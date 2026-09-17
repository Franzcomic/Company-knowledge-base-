import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getMe, type UserInfo } from '@/api/auth'
import { getToken, setToken, clearToken } from '@/utils/request'

/** 登录态：token + 当前用户信息 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => {
    const code = userInfo.value?.roleCode
    return code === 'SYS_ADMIN' || code === 'DEPT_ADMIN'
  })

  /** 登录：成功则落 token 与用户信息 */
  async function login(params: { username: string; password: string }) {
    const res = await loginApi(params)
    token.value = res.token
    userInfo.value = res.user
    setToken(res.token)
    return res.user
  }

  /** 拉取当前用户信息（路由守卫在刷新后恢复会话用） */
  async function fetchMe() {
    const me = await getMe()
    userInfo.value = me
    return me
  }

  /** 退出：调用后端并清理本地状态 */
  async function logout() {
    try {
      await logoutApi()
    } finally {
      reset()
    }
  }

  /** 本地清理（登出 / 401） */
  function reset() {
    token.value = null
    userInfo.value = null
    clearToken()
  }

  return { token, userInfo, isLoggedIn, isAdmin, login, fetchMe, logout, reset }
})