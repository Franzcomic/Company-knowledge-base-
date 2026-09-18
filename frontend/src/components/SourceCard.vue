<script setup lang="ts">
// 来源片段卡片（模块④⑤）：展示 document_id/title/chunk/相似度，可点击预览文档分块
import { ref } from 'vue'
import { getDocumentChunks } from '@/api/kb'
import type { Source } from '@/api/conversation'
import { renderMarkdown } from '@/utils/markdown'

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
    // 文档分块接口后端阶段 4 已实现，直接拉取定位分块内容
    const chunks = await getDocumentChunks(props.source.documentId)
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
          <div v-else class="preview-content md-preview" v-html="renderMarkdown(chunkContent)"></div>
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
/* markdown 渲染后的预览排版 */
.md-preview {
  white-space: normal;
}
.md-preview :deep(h1),
.md-preview :deep(h2),
.md-preview :deep(h3),
.md-preview :deep(h4) {
  margin: 10px 0 6px;
  font-weight: 600;
  line-height: 1.4;
}
.md-preview :deep(p) {
  margin: 6px 0;
}
.md-preview :deep(ul),
.md-preview :deep(ol) {
  margin: 6px 0;
  padding-left: 22px;
}
.md-preview :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  border-radius: 6px;
  padding: 10px 12px;
  margin: 8px 0;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.5;
}
.md-preview :deep(code:not([class])) {
  background: #d9d9d9;
  border-radius: 3px;
  padding: 1px 5px;
  font-size: 13px;
  font-family: Consolas, 'Courier New', monospace;
}
.md-preview :deep(pre) code {
  background: transparent;
  padding: 0;
  color: inherit;
}
.md-preview :deep(blockquote) {
  margin: 8px 0;
  padding: 4px 12px;
  border-left: 3px solid var(--el-border-color);
  color: var(--el-text-color-secondary);
}
.md-preview :deep(a) {
  color: var(--el-color-primary);
  text-decoration: none;
}
.md-preview :deep(table) {
  border-collapse: collapse;
  margin: 8px 0;
}
.md-preview :deep(th),
.md-preview :deep(td) {
  border: 1px solid var(--el-border-color-lighter);
  padding: 5px 10px;
}
.md-preview :deep(th) {
  background: #f0f0f0;
}
.md-preview :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}
.preview-empty {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  text-align: center;
  padding: 24px 0;
}
</style>
