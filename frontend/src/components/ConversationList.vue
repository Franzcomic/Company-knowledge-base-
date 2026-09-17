<script setup lang="ts">
// 会话侧边栏（模块⑥⑦）：新建 / 切换 / 删除
import { ChatDotRound, Delete, Plus } from '@element-plus/icons-vue'
import type { Conversation } from '@/api/conversation'

defineProps<{
  items: Conversation[]
  currentId: number | null
  loading?: boolean
}>()

const emit = defineEmits<{
  create: []
  select: [id: number]
  delete: [id: number]
}>()
</script>

<template>
  <div class="conv-panel">
    <div class="conv-header">
      <span class="conv-title">会话</span>
      <el-button type="primary" :icon="Plus" size="small" @click="emit('create')">新建会话</el-button>
    </div>

    <div v-loading="loading" class="conv-list">
      <el-empty
        v-if="!loading && items.length === 0"
        :image-size="60"
        description="暂无会话，点击「新建会话」开始提问"
      />
      <div
        v-for="item in items"
        :key="item.id"
        class="conv-item"
        :class="{ active: item.id === currentId }"
        @click="emit('select', item.id)"
      >
        <el-icon class="conv-item-icon"><ChatDotRound /></el-icon>
        <span class="conv-item-title" :title="item.title || '新会话'">
          {{ item.title || '新会话' }}
        </span>
        <el-icon
          class="conv-item-delete"
          @click.stop="emit('delete', item.id)"
        >
          <Delete />
        </el-icon>
      </div>
    </div>
  </div>
</template>

<style scoped>
.conv-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-right: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
}
.conv-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.conv-title {
  font-size: 15px;
  font-weight: 600;
}
.conv-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.conv-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 4px;
  color: var(--el-text-color-primary);
  transition: background 0.2s;
}
.conv-item:hover {
  background: var(--el-fill-color-light);
}
.conv-item.active {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}
.conv-item-icon {
  flex: none;
  font-size: 15px;
}
.conv-item-title {
  flex: 1;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.conv-item-delete {
  flex: none;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  opacity: 0;
  transition: opacity 0.2s;
}
.conv-item:hover .conv-item-delete {
  opacity: 1;
}
.conv-item-delete:hover {
  color: var(--el-color-danger);
}
</style>
