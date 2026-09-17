import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    redirect: '/chat',
    children: [
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: { title: '智能问答', requiresAuth: true }
      },
      {
        path: 'kb',
        name: 'Kb',
        component: () => import('@/views/kb/KbView.vue'),
        meta: { title: '知识库', requiresAuth: true }
      }
    ]
  },
  // 未匹配路由兜底
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/** 全局前置守卫：未登录 → 登录页；已登录进入登录页 → 首页 */
router.beforeEach(async (to) => {
  const hasToken = !!getToken()
  const userStore = useUserStore()

  // 已登录访问 /login 时，恢复会话并回首页
  if (hasToken && to.name === 'Login') {
    if (!userStore.userInfo) {
      try {
        await userStore.fetchMe()
      } catch {
        userStore.reset()
      }
    }
    return { path: '/' }
  }

  // 需要登录但没有 token：跳登录
  if (to.meta.requiresAuth && !hasToken) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  // 进入受保护页且未加载用户信息时拉取
  if (hasToken && to.meta.requiresAuth && !userStore.userInfo) {
    try {
      await userStore.fetchMe()
    } catch {
      userStore.reset()
      return { name: 'Login' }
    }
  }

  document.title = to.meta.title ? `${to.meta.title} · 企业智能知识问答` : '企业智能知识问答'
  return true
})

export default router