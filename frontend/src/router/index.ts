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
      },
      {
        path: 'kb/:id/documents',
        name: 'KbDocuments',
        component: () => import('@/views/kb/KbDocuments.vue'),
        meta: { title: '文档管理', requiresAuth: true }
      },
      // 管理端（阶段4 P1，仅 SYS_ADMIN 可见）
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', requiresAuth: true, roles: ['SYS_ADMIN'] }
      },
      {
        path: 'admin/roles',
        name: 'RoleManage',
        component: () => import('@/views/admin/RoleManage.vue'),
        meta: { title: '角色管理', requiresAuth: true, roles: ['SYS_ADMIN'] }
      },
      {
        path: 'admin/departments',
        name: 'DepartmentManage',
        component: () => import('@/views/admin/DepartmentManage.vue'),
        meta: { title: '部门管理', requiresAuth: true, roles: ['SYS_ADMIN'] }
      },
      {
        path: 'admin/stats',
        name: 'StatsDashboard',
        component: () => import('@/views/admin/StatsDashboard.vue'),
        meta: { title: '统计看板', requiresAuth: true, roles: ['SYS_ADMIN'] }
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

  // 角色权限校验：路由声明 roles 时，当前用户角色不在白名单内则回首页（前端“可见”维度，数据层由后端 Filter 兜底）
  const roles = to.meta.roles as string[] | undefined
  if (roles && userStore.userInfo && !roles.includes(userStore.userInfo.roleCode ?? '')) {
    return { path: '/' }
  }

  document.title = to.meta.title ? `${to.meta.title} · 企业智能知识问答` : '企业智能知识问答'
  return true
})

export default router