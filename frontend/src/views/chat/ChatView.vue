<script setup lang="ts">
// 智能问答主界面（模块⑤⑥⑦，阶段 3 核心）：
// 会话侧边栏 + 多轮消息流 + 来源展示 + 输入发送 + 历史续聊
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ElInput } from 'element-plus'
import { Promotion, MagicStick } from '@element-plus/icons-vue'
import ConversationList from '@/components/ConversationList.vue'
import ChatMessage, { type ChatMessageItem } from '@/components/ChatMessage.vue'
import {
  listConversations,
  createConversation,
  deleteConversation,
  getConversationMessages,
  sendMessage,
  type Conversation
} from '@/api/conversation'

const conversations = ref<Conversation[]>([])
const currentId = ref<number | null>(null)
const messages = ref<ChatMessageItem[]>([])
const loadingList = ref(false)
const loadingHistory = ref(false)
const sending = ref(false)
const draft = ref('')
const suggestions = ['年假如何申请？', '出差报销需要哪些材料？', '如何连接公司 VPN？', '新员工入职需要准备什么？']
const inputRef = ref<InstanceType<typeof ElInput> | null>(null)
const msgListRef = ref<HTMLElement | null>(null)

let uidSeed = 0
const nextUid = () => ++uidSeed

const currentTitle = computed(
  () => conversations.value.find((c) => c.id === currentId.value)?.title || '新会话'
)
const canSend = computed(() => draft.value.trim().length > 0 && !sending.value)

/* ---------------- 列表 / 历史 ---------------- */

async function loadConversations() {
  loadingList.value = true
  try {
    conversations.value = await listConversations()
    // 默认选中最近会话（列表按 id 倒序，首个即最新）
    if (currentId.value == null && conversations.value.length > 0) {
      await selectConversation(conversations.value[0].id)
    }
  } finally {
    loadingList.value = false
  }
}

async function selectConversation(id: number) {
  if (sending.value) return
  if (currentId.value === id && messages.value.length > 0) return
  currentId.value = id
  messages.value = []
  loadingHistory.value = true
  try {
    const list = await getConversationMessages(id)
    messages.value = list.map((m) => ({
      uid: nextUid(),
      id: m.id,
      role: m.role,
      content: m.content,
      sources: m.sources || null,
      answered: m.answered,
      createdAt: m.createdAt
    }))
  } catch {
    messages.value = []
  } finally {
    loadingHistory.value = false
    await scrollToBottom()
  }
}

async function createNewConversation() {
  if (sending.value) return
  const conv = await createConversation()
  conversations.value.unshift(conv)
  currentId.value = conv.id
  messages.value = []
  await scrollToBottom()
  inputRef.value?.focus()
}

async function handleDelete(id: number) {
  if (sending.value) return
  try {
    await ElMessageBox.confirm('确定删除该会话吗？其全部问答记录将一并清除。', '删除会话', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteConversation(id)
  ElMessage.success('会话已删除')
  const idx = conversations.value.findIndex((c) => c.id === id)
  if (idx >= 0) conversations.value.splice(idx, 1)
  if (currentId.value === id) {
    currentId.value = null
    messages.value = []
    // 删除当前会话后自动切到剩余最近会话
    if (conversations.value.length > 0) {
      await selectConversation(conversations.value[0].id)
    }
  }
}

/* ---------------- 发送 / 多轮 ---------------- */

async function send() {
  const text = draft.value.trim()
  if (!text || sending.value) return
  if (text.length > 2000) { ElMessage.warning('问题最多 2000 字'); return }
  sending.value = true
  draft.value = ''

  // 无会话时先建会话（首条消息后后端自动填充标题）
  let convId = currentId.value
  const isFirstExchange = !messages.value.some((m) => m.role === 'ASSISTANT')
  const placeholderUid = nextUid()
  try {
    if (convId == null) {
      const conv = await createConversation()
      conversations.value.unshift(conv)
      currentId.value = conv.id
      convId = conv.id
    }
    messages.value.push({ uid: nextUid(), role: 'USER', content: text })
    messages.value.push({ uid: placeholderUid, role: 'ASSISTANT', content: '', loading: true })
    await scrollToBottom()
    const res = await sendMessage({ conversationId: convId, content: text })
    replaceMessage(placeholderUid, {
      uid: placeholderUid,
      id: res.messageId,
      role: 'ASSISTANT',
      content: res.content,
      sources: res.sources || null,
      answered: res.answered
    })
    // 首轮问答后会话标题被后端自动填充，刷新列表标题
    if (isFirstExchange) {
      await loadConversations()
    }
  } catch {
    draft.value = text
    replaceMessage(placeholderUid, {
      uid: placeholderUid,
      role: 'ASSISTANT',
      content: '回答失败，请稍后重试',
      error: true
    })
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

function replaceMessage(uid: number, item: ChatMessageItem) {
  const idx = messages.value.findIndex((m) => m.uid === uid)
  if (idx >= 0) messages.value[idx] = item
}

/* ---------------- 滚动 ---------------- */

async function scrollToBottom() {
  await nextTick()
  const el = msgListRef.value
  if (el) el.scrollTop = el.scrollHeight
}

// 消息变化 / 发送中自动滚到底部
watch([() => messages.value.length, sending], scrollToBottom)

onMounted(loadConversations)
</script>

<template>
  <div class="chat-page">
    <el-container class="chat-container">
      <el-aside width="260px" class="chat-aside">
        <ConversationList
          :items="conversations"
          :current-id="currentId"
          :loading="loadingList"
          @create="createNewConversation"
          @select="selectConversation"
          @delete="handleDelete"
        />
      </el-aside>

      <el-main class="chat-main">
        <div class="chat-topbar">
          <span class="chat-conv-title">{{ currentTitle }}</span>
        </div>

        <!-- 消息区 -->
        <div ref="msgListRef" class="msg-list">
          <div v-if="loadingHistory" v-loading="true" class="msg-loading-area" />
          <template v-else>
            <el-empty
              v-if="messages.length === 0"
              class="msg-empty"
              :image-size="120"
              description=" "
            >
              <el-icon class="msg-empty-icon"><MagicStick /></el-icon>
              <div class="msg-empty-title">企业智能知识问答</div>
              <div class="msg-empty-sub">
                向企业知识库提问，AI 将基于内部文档生成带来源的回答
              </div>
              <div class="msg-empty-tip">Enter 发送 · Shift+Enter 换行</div>
              <div class="suggestions" aria-label="常见问题">
                <el-button v-for="question in suggestions" :key="question" @click="draft = question; inputRef?.focus()">
                  {{ question }}
                </el-button>
              </div>
            </el-empty>
            <div v-for="m in messages" :key="m.uid" class="msg-row">
              <ChatMessage :message="m" />
            </div>
          </template>
        </div>

        <!-- 输入区 -->
        <div class="chat-input">
          <el-input
            ref="inputRef"
            v-model="draft"
            type="textarea"
            resize="none"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请输入你的问题…"
            maxlength="2000"
            @keydown.enter.exact.prevent="send"
          />
          <div class="chat-input-bar">
            <span class="chat-input-hint">Enter 发送 · Shift+Enter 换行</span>
            <el-button
              type="primary"
              :icon="Promotion"
              :loading="sending"
              :disabled="!canSend"
              @click="send"
            >
              发送
            </el-button>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.suggestions { display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; max-width: 600px; margin: 20px auto; }
.suggestions .el-button { margin: 0; }
.chat-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.chat-container {
  height: 100%;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  overflow: hidden;
  background: var(--el-bg-color);
}
.chat-aside {
  border-right: 1px solid var(--el-border-color-light);
  padding: 0;
  overflow: hidden;
}
.chat-main {
  display: flex;
  flex-direction: column;
  padding: 0;
  height: 100%;
  overflow: hidden;
}
.chat-topbar {
  flex: none;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 15px;
  font-weight: 600;
}
.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}
.msg-loading-area {
  height: 100%;
  min-height: 120px;
}
.msg-empty {
  margin-top: 10vh;
}
.msg-empty-icon {
  font-size: 48px;
  color: var(--el-color-primary-light-5);
  margin-bottom: 12px;
}
.msg-empty-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}
.msg-empty-sub {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 16px;
}
.msg-empty-tip {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
.msg-row {
  max-width: 960px;
  margin: 0 auto;
}
.chat-input {
  flex: none;
  padding: 12px 20px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  max-width: 960px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}
.chat-input-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}
.chat-input-hint {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
</style>
