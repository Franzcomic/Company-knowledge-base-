<script setup lang="ts">
// 消息气泡（模块⑤⑥）：用户 / AI 消息、来源引用、拒答标识、赞/踩评价（P1）
import { computed, reactive } from 'vue'
import MarkdownIt from 'markdown-it'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CaretBottom, CaretTop, CircleClose, Loading } from '@element-plus/icons-vue'
import SourceCard from '@/components/SourceCard.vue'
import { submitFeedback, type Source } from '@/api/conversation'

// Markdown 解析器：禁原生 HTML，规避 XSS；返回答案由后端生成，可安全渲染
const md = new MarkdownIt({ html: false, linkify: true })
function renderMarkdown(content: string) {
  return md.render(content)
}

/** 会话内消息项（历史消息 + 本地即时渲染消息统一形态） */
export interface ChatMessageItem {
  /** 本地唯一标识（用于列表渲染 key 与占位替换） */
  uid: number
  id?: number | null
  role: 'USER' | 'ASSISTANT'
  content: string
  sources?: Source[] | null
  answered?: boolean | null
  createdAt?: string | null
  loading?: boolean
  error?: boolean
}

const props = defineProps<{ message: ChatMessageItem }>()

// 已评价记录：模块级共享，切换会话 / 重新渲染后仍保留本次会话内的评价状态
const ratings = reactive(new Map<number, 'UP' | 'DOWN'>())
const rating = computed<'UP' | 'DOWN' | null>(() =>
  props.message.id != null ? (ratings.get(props.message.id) ?? null) : null
)

async function rate(value: 'UP' | 'DOWN') {
  const id = props.message.id
  if (id == null || rating.value) return
  let reason = ''
  if (value === 'DOWN') {
    try {
      const { value: text } = await ElMessageBox.prompt(
        '请简要说明回答存在的问题，帮助我们改进',
        '反馈',
        {
          inputType: 'textarea',
          inputPlaceholder: '选填',
          confirmButtonText: '提交',
          cancelButtonText: '取消'
        }
      )
      reason = text ?? ''
    } catch {
      return // 用户取消
    }
  }
  try {
    // 评价接口后端阶段 4 已实现；失败时拦截器已统一弹错
    await submitFeedback(id, { rating: value, reason: reason || undefined })
    ratings.set(id, value)
    ElMessage.success('感谢你的反馈')
  } catch {
    // 错误提示已由拦截器统一处理
  }
}
</script>

<template>
  <div class="chat-message" :class="message.role === 'USER' ? 'is-user' : 'is-assistant'">
    <div class="msg-avatar">{{ message.role === 'USER' ? '我' : 'AI' }}</div>
    <div class="msg-body">
      <div class="msg-bubble">
        <!-- 生成中占位 -->
        <template v-if="message.loading">
          <span class="msg-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            正在检索并生成回答…
          </span>
        </template>

        <!-- 发送失败占位 -->
        <template v-else-if="message.error">
          <el-icon class="msg-error-icon"><CircleClose /></el-icon>
          <span>{{ message.content }}</span>
        </template>

        <template v-else>
          <!-- 用户消息按纯文本展示，AI 回答按 Markdown 渲染 -->
          <div
            v-if="message.role === 'ASSISTANT'"
            class="msg-text md-body"
            v-html="renderMarkdown(message.content)"
          ></div>
          <div v-else class="msg-text">{{ message.content }}</div>
          <div v-if="message.role === 'ASSISTANT' && message.answered === false" class="msg-refused">
            未在知识库中找到足够可靠的信息（已拒答）
          </div>
          <div v-else-if="message.sources && message.sources.length" class="msg-sources">
            <div class="msg-sources-label">来源（{{ message.sources.length }}）</div>
            <div class="msg-sources-list">
              <SourceCard v-for="(s, i) in message.sources" :key="`${message.id}-${i}`" :source="s" />
            </div>
          </div>
        </template>
      </div>

      <!-- P1：回答评价 -->
      <div
        v-if="message.role === 'ASSISTANT' && !message.loading && message.id != null"
        class="msg-actions"
      >
        <el-tooltip content="回答有帮助" placement="top">
          <el-button
            link
            :icon="CaretTop"
            :type="rating === 'UP' ? 'success' : ''"
            :disabled="!!rating"
            @click="rate('UP')"
          />
        </el-tooltip>
        <el-tooltip content="回答有误" placement="top">
          <el-button
            link
            :icon="CaretBottom"
            :type="rating === 'DOWN' ? 'danger' : ''"
            :disabled="!!rating"
            @click="rate('DOWN')"
          />
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-message {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
}
.chat-message.is-user {
  flex-direction: row-reverse;
}
.msg-avatar {
  flex: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #fff;
}
.is-user .msg-avatar {
  background: var(--el-color-primary);
}
.is-assistant .msg-avatar {
  background: var(--el-color-success);
}
.msg-body {
  max-width: 72%;
  display: flex;
  flex-direction: column;
}
.is-user .msg-body {
  align-items: flex-end;
}
.msg-bubble {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
}
.is-user .msg-bubble {
  background: var(--el-color-primary-light-8);
  border-color: var(--el-color-primary-light-7);
}
.msg-text {
  white-space: pre-wrap;
}
/* AI 回答：markdown 渲染后的排版 */
.msg-text.md-body {
  white-space: normal;
}
.md-body :deep(h1),
.md-body :deep(h2),
.md-body :deep(h3),
.md-body :deep(h4) {
  margin: 12px 0 6px;
  font-weight: 600;
  line-height: 1.4;
}
.md-body :deep(h1) {
  font-size: 19px;
}
.md-body :deep(h2) {
  font-size: 17px;
}
.md-body :deep(h3) {
  font-size: 15px;
}
.md-body :deep(p) {
  margin: 6px 0;
}
.md-body :deep(ul),
.md-body :deep(ol) {
  margin: 6px 0;
  padding-left: 22px;
}
.md-body :deep(li) {
  margin: 3px 0;
}
.md-body :deep(strong) {
  font-weight: 600;
}
.md-body :deep(code:not([class])) {
  background: var(--el-fill-color-light);
  border-radius: 3px;
  padding: 1px 5px;
  font-size: 13px;
  font-family: Consolas, 'Courier New', monospace;
}
.md-body :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  border-radius: 6px;
  padding: 10px 12px;
  margin: 8px 0;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.5;
}
.md-body :deep(pre) code {
  background: transparent;
  padding: 0;
  color: inherit;
}
.md-body :deep(blockquote) {
  margin: 8px 0;
  padding: 4px 12px;
  border-left: 3px solid var(--el-border-color);
  color: var(--el-text-color-secondary);
}
.md-body :deep(a) {
  color: var(--el-color-primary);
  text-decoration: none;
}
.md-body :deep(a):hover {
  text-decoration: underline;
}
.md-body :deep(table) {
  border-collapse: collapse;
  margin: 8px 0;
}
.md-body :deep(th),
.md-body :deep(td) {
  border: 1px solid var(--el-border-color-lighter);
  padding: 5px 10px;
}
.md-body :deep(th) {
  background: var(--el-fill-color-light);
}
.md-body :deep(hr) {
  border: none;
  border-top: 1px solid var(--el-border-color-lighter);
  margin: 10px 0;
}
.md-body :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}
.msg-loading {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--el-text-color-secondary);
}
.msg-error-icon {
  vertical-align: -2px;
  color: var(--el-color-danger);
  margin-right: 4px;
}
.msg-refused {
  margin-top: 8px;
  display: inline-block;
  font-size: 12px;
  color: var(--el-color-warning);
  background: var(--el-color-warning-light-9);
  border-radius: 4px;
  padding: 2px 8px;
}
.msg-sources {
  margin-top: 10px;
}
.msg-sources-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}
.msg-sources-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.msg-actions {
  display: flex;
  gap: 2px;
  margin-top: 2px;
}
</style>
