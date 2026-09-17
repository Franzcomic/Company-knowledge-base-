<script setup lang="ts">
// 部门管理页（阶段4 P1，仅 SYS_ADMIN 可见）：只读列表（后端仅提供 GET /api/departments）
import { ref, onMounted } from 'vue'
import { listDepartments, type Department } from '@/api/kb'

const loading = ref(false)
const departments = ref<Department[]>([])

onMounted(async () => {
  loading.value = true
  try {
    departments.value = await listDepartments()
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="page-header">
        <span class="page-title">部门管理</span>
        <el-tag type="info" size="small" effect="plain">只读 · 部门增删改由后端数据层维护</el-tag>
      </div>
    </template>

    <el-table v-loading="loading" :data="departments" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="name" label="部门名称" min-width="160" />
      <el-table-column prop="parentId" label="上级部门 ID" width="120" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <template #empty>
        <el-empty description="暂无部门" />
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
