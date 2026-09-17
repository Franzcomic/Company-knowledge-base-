<script setup lang="ts">
// 用户管理页（阶段4 P1，仅 SYS_ADMIN）：列表 / 编辑部门·角色 / 启停用
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listUsers, updateUser, listRoles, type UserItem, type Role } from '@/api/user'
import { listDepartments, type Department } from '@/api/kb'

const loading = ref(false)
const users = ref<UserItem[]>([])
const departments = ref<Department[]>([])
const roles = ref<Role[]>([])

const dialogVisible = ref(false)
const submitting = ref(false)
const current = ref<UserItem | null>(null)
const form = reactive<{ departmentId: number | null; roleId: number | null; status: number }>({
  departmentId: null,
  roleId: null,
  status: 1
})

function departmentName(id?: number | null) {
  return departments.value.find((d) => d.id === id)?.name || '全司'
}

async function fetchUsers() {
  loading.value = true
  try {
    users.value = await listUsers()
  } finally {
    loading.value = false
  }
}

function openEdit(row: UserItem) {
  current.value = row
  form.departmentId = row.departmentId ?? null
  form.roleId = row.roleId ?? null
  form.status = row.status ?? 1
  dialogVisible.value = true
}

/** 行内快捷启停用 */
async function toggleStatus(row: UserItem) {
  await updateUser(row.id, { status: row.status === 1 ? 0 : 1 })
  row.status = row.status === 1 ? 0 : 1
  ElMessage.success(row.status === 1 ? '已启用' : '已停用')
}

async function submit() {
  if (!current.value) return
  if (form.roleId == null) {
    ElMessage.warning('请选择角色')
    return
  }
  submitting.value = true
  try {
    await updateUser(current.value.id, {
      departmentId: form.departmentId,
      roleId: form.roleId,
      status: form.status
    })
    ElMessage.success('用户已更新')
    dialogVisible.value = false
    await fetchUsers()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await fetchUsers()
  try {
    ;[departments.value, roles.value] = await Promise.all([listDepartments(), listRoles()])
  } catch {
    // 下拉缺失时仅影响表单选项，列表仍可用
  }
})
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="page-header">
        <span class="page-title">用户管理</span>
        <el-tag type="info" size="small" effect="plain">仅系统管理员可见</el-tag>
      </div>
    </template>

    <el-table v-loading="loading" :data="users" stripe>
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="110" />
      <el-table-column label="部门" width="140">
        <template #default="{ row }">{{ departmentName(row.departmentId) }}</template>
      </el-table-column>
      <el-table-column label="角色" width="130">
        <template #default="{ row }">
          <el-tag size="small" effect="plain" :type="row.roleCode === 'SYS_ADMIN' ? 'danger' : 'primary'">
            {{ row.roleName || row.roleCode }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无用户" />
      </template>
    </el-table>
  </el-card>

  <!-- 编辑用户：分配部门 / 角色 / 启停用 -->
  <el-dialog v-model="dialogVisible" title="编辑用户" width="440px" destroy-on-close>
    <el-form label-width="90px">
      <el-form-item label="用户名">
        <span>{{ current?.username }}</span>
      </el-form-item>
      <el-form-item label="所属部门">
        <el-select v-model="form.departmentId" placeholder="不选则为全司" clearable style="width: 100%">
          <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色" required>
        <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
          <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
    </template>
  </el-dialog>
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
