<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menuItems = [
  { key: '/chat', label: '智能问答' },
  { key: '/kb', label: '知识库' }
]

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="200px" class="layout-aside">
      <div class="logo">企业智能知识问答</div>
      <el-menu :default-active="route.path" router class="layout-menu">
        <el-menu-item v-for="item in menuItems" :key="item.key" :index="item.key">
          {{ item.label }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <span class="header-title">{{ route.meta.title }}</span>
        <el-dropdown>
          <span class="header-user">
            {{ userStore.userInfo?.realName || userStore.userInfo?.username || '用户' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}
.layout-aside {
  border-right: 1px solid var(--el-border-color-light);
}
.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  font-weight: 600;
  font-size: 14px;
  color: var(--el-color-primary);
}
.layout-menu {
  border-right: none;
}
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color-light);
}
.header-title {
  font-weight: 600;
}
.header-user {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.layout-main {
  background: var(--el-bg-color-page);
}
</style>