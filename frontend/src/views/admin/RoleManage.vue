<script setup lang="ts">
// 角色管理页（阶段4 P1，仅 SYS_ADMIN 可见）：只读列表（后端仅提供 GET /api/roles）
import { ref, onMounted } from 'vue'
import { listRoles, type Role } from '@/api/user'

const loading = ref(false)
const roles = ref<Role[]>([])

onMounted(async () => {
  loading.value = true
  try {
    roles.value = await listRoles()
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="page-header">
        <span class="page-title">角色管理</span>
        <el-tag type="info" size="small" effect="plain">只读 · 角色增删改由后端数据层维护</el-tag>
      </div>
    </template>

    <el-table v-loading="loading" :data="roles" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="code" label="角色编码" min-width="140" />
      <el-table-column prop="name" label="角色名称" min-width="160" />
      <template #empty>
        <el-empty description="暂无角色" />
      </template>
    </el-table>
  </el-card>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-title {
  font-weight: 600;
}
</style>
