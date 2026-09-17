<script setup lang="ts">
// 来源片段卡片（模块④⑤）：展示 document_id/title/chunk/相似度，可点击预览文档分块
import { ref } from 'vue'
import { getDocumentChunks } from '@/api/kb'
import type { Source } from '@/api/conversation'

const props = defineProps<{ source: Source }>()

const previewVisible = ref(false)
const previewLoading = ref(false)
const chunkContent = ref('')
const contentUnavailable = ref(false)

/** 从 chunkId（后端格式 doc-{documentId}-{index}）解析分块序号 */
function chunkIndexFromId(chunkId: string): number | null {
  const parts = chunkId.split('-')
  const last = Number(parts[parts.length - 1])
  return parts.length >= 3 && Number.isFinite(last) ? last : null
}

async function openPreview() {
  previewVisible.value = true
  previewLoading.value = true
  chunkContent.value = ''
  contentUnavailable.value = false
  try {
    // 文档分块接口为 P1（后端阶段 4 提供），暂不可用时静默降级为仅元数据
    const chunks = await getDocumentChunks(props.source.documentId, { silent: true })
    const idx = chunkIndexFromId(props.source.chunkId)
    const hit = idx != null ? chunks[idx] : undefined
    if (hit) {
      chunkContent.value = hit.content
    } else {
      contentUnavailable.value = true
    }
  } catch {
    contentUnavailable.value = true
  } finally {
    previewLoading.value = false
  }
}
</script>

<template>
  <div class="source-card" title="点击查看来源分块" @click="openPreview">
    <div class="source-title" :title="source.title || '未命名文档'">
      {{ source.title || '未命名文档' }}
    </div>
    <div class="source-meta">
      <span class="source-id">文档 #{{ source.documentId }}</span>
      <el-tag size="small" type="info" effect="plain">相似度 {{ source.similarity.toFixed(2) }}</el-tag>
    </div>

    <el-dialog
      v-model="previewVisible"
      width="560px"
      :title="`来源预览 · ${source.title || '未命名文档'}`"
      append-to-body
    >
      <div v-loading="previewLoading" class="preview-body">
        <div class="preview-meta">
          <span>文档 ID：{{ source.documentId }}</span>
          <span>分块：{{ source.chunkId }}</span>
          <el-tag size="small" type="info" effect="plain">
            相似度 {{ source.similarity.toFixed(4) }}
          </el-tag>
        </div>
        <template v-if="!previewLoading">
          <div v-if="contentUnavailable" class="preview-empty">分块内容暂不可用</div>
          <div v-else class="preview-content">{{ chunkContent }}</div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.source-card {
  display: inline-flex;
  flex-direction: column;
  gap: 4px;
  max-width: 260px;
  padding: 6px 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}
.source-card:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-fill-color-light);
}
.source-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.source-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}
.source-id {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.preview-body {
  min-height: 120px;
}
.preview-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 12px;
}
.preview-content {
  font-size: 13px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  padding: 12px;
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
}
.preview-empty {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  text-align: center;
  padding: 24px 0;
}
</style>
