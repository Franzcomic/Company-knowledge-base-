import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

/** 扩展 axios 配置：silent=true 时请求失败不弹错误提示（调用方自行处理降级） */
declare module 'axios' {
  export interface AxiosRequestConfig {
    silent?: boolean
  }
}

/** 后端统一返回结构：{ code, msg, data } */
export interface ApiResult<T = unknown> {
  code: number
  msg: string
  data: T
}

/** token 本地存储 key */
const TOKEN_KEY = 'corpedia_token'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

/** 解析请求 baseURL：直连或走代理 */
function resolveBaseURL(): string {
  return import.meta.env.VITE_API_BASE_URL || '/api'
}

/** 401 跳登录的防抖标记，避免连续 401 弹多次消息 */
let redirecting = false
function redirectToLogin(): void {
  clearToken()
  if (redirecting) return
  redirecting = true
  ElMessage.warning('登录已过期，请重新登录')
  window.location.href = '/login'
}

const service: AxiosInstance = axios.create({
  baseURL: resolveBaseURL(),
  timeout: 30000
})

// 请求拦截：注入 JWT
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers = config.headers ?? {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：解包 {code,msg,data}，统一处理 401 与业务错误
service.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const res = response.data
    if (res.code === 200) {
      // axios 类型期望 AxiosResponse，这里统一返回 data 字段（ApiResult），做类型断言
      return res as unknown as AxiosResponse
    }
    if (res.code === 401) {
      redirectToLogin()
      return Promise.reject(new Error(res.msg || '未授权'))
    }
    if (!response.config.silent) {
      ElMessage.error(res.msg || '请求失败')
    }
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      redirectToLogin()
    } else if (!error?.config?.silent) {
      ElMessage.error(error?.response?.data?.msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

/** 泛型请求封装：直接返回 data 字段 */
async function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  const res = (await service.request(config)) as unknown as ApiResult<T>
  return res.data
}

export const http = {
  get<T = unknown>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig) {
    return request<T>({ ...config, url, method: 'get', params })
  },
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return request<T>({ ...config, url, method: 'post', data })
  },
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return request<T>({ ...config, url, method: 'put', data })
  },
  delete<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return request<T>({ ...config, url, method: 'delete' })
  }
}

export default service