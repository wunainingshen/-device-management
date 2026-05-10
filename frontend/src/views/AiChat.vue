<template>
  <div class="ai-page">
    <div class="ai-container">
      <div class="ai-header">
        <div class="ai-header-left">
          <div class="ai-logo">
            <el-icon :size="18"><MagicStick /></el-icon>
          </div>
          <div>
            <div class="ai-title">AI 智能助手</div>
            <div class="ai-subtitle">由 DeepSeek 驱动</div>
          </div>
        </div>
        <el-tag size="small" effect="dark" round>DeepSeek</el-tag>
      </div>

      <div class="ai-messages" ref="messagesRef">
        <div class="ai-welcome" v-if="messages.length === 0">
          <div class="welcome-icon"><el-icon :size="48"><MagicStick /></el-icon></div>
          <h3>你好！我是 AI 智能助手</h3>
          <p>我可以帮助你解答设备管理相关的问题，或者进行日常对话</p>
          <div class="suggestions">
            <div v-for="(s, i) in suggestions" :key="i" class="suggestion-chip" @click="sendQuestion(s)">
              {{ s }}
            </div>
          </div>
        </div>

        <div v-for="(msg, idx) in messages" :key="idx" class="ai-message" :class="msg.role">
          <div class="msg-avatar" v-if="msg.role === 'assistant'">
            <el-avatar :size="34" style="background:linear-gradient(135deg,#06b6d4,#0891b2)">
              <el-icon><MagicStick /></el-icon>
            </el-avatar>
          </div>
          <div class="msg-avatar" v-else>
            <el-avatar :size="34" style="background:linear-gradient(135deg,#10b981,#059669)">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <div class="msg-body">
            <div class="msg-role">{{ msg.role === 'assistant' ? 'AI 助手' : '你' }}</div>
            <div class="msg-content" v-html="renderContent(msg.content)"></div>
          </div>
        </div>

        <div v-if="loading" class="ai-message assistant">
          <div class="msg-avatar">
            <el-avatar :size="34" style="background:linear-gradient(135deg,#06b6d4,#0891b2)">
              <el-icon><MagicStick /></el-icon>
            </el-avatar>
          </div>
          <div class="msg-body">
            <div class="msg-role">AI 助手</div>
            <div class="typing-dots">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>
      </div>

      <div class="ai-input-area">
        <el-input v-model="inputText" type="textarea" :rows="2" placeholder="输入您的问题..." @keyup.enter.prevent="sendMessage" :disabled="loading" class="ai-input" />
        <div class="input-footer">
          <span class="input-hint">按 Enter 发送</span>
          <el-button type="primary" @click="sendMessage" :loading="loading" :disabled="!inputText.trim()" round>发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { MagicStick, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'

const messagesRef = ref(null)
const inputText = ref('')
const loading = ref(false)
const messages = ref([])
const history = ref([])

const suggestions = ['如何管理设备？', '帮我分析设备维护建议', '什么是好的设备管理实践？', '介绍一下设备管理系统的功能']

const scrollToBottom = () => { nextTick(() => { if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight }) }

const sendMessage = async () => {
  const t = inputText.value.trim()
  if (!t || loading.value) return
  messages.value.push({ role: 'user', content: t })
  history.value.push({ role: 'user', content: t })
  inputText.value = ''; scrollToBottom()
  loading.value = true
  try {
    const res = await axios.post('/api/ai/chat', { message: t, history: history.value.slice(-20) })
    if (res.data.code === 200) { const r = res.data.data; messages.value.push({ role: 'assistant', content: r }); history.value.push({ role: 'assistant', content: r }) }
    else throw new Error(res.data.message)
  } catch (e) { ElMessage.error(e.message || 'AI服务暂时不可用') }
  finally { loading.value = false; scrollToBottom() }
}

const sendQuestion = (q) => { inputText.value = q; sendMessage() }

const renderContent = (content) => {
  if (!content) return ''
  return content
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')
}

watch(messages, () => scrollToBottom(), { deep: true })
</script>

<style scoped>
.ai-page {
  max-width: 900px; margin: 0 auto; height: calc(100vh - 140px);
  animation: fadeSlideUp 0.5s ease-out;
}
@keyframes fadeSlideUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }

.ai-container {
  height: 100%; display: flex; flex-direction: column;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.ai-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 24px; border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.ai-header-left { display: flex; align-items: center; gap: 12px; }

.ai-logo {
  width: 36px; height: 36px; border-radius: 10px;
  background: linear-gradient(135deg, #06b6d4, #0891b2);
  display: flex; align-items: center; justify-content: center; color: #fff;
}

.ai-title { font-size: 16px; font-weight: 600; color: var(--text-primary); }
.ai-subtitle { font-size: 12px; color: var(--text-muted); }

.ai-messages { flex: 1; overflow-y: auto; padding: 20px 16px; }

.ai-welcome { text-align: center; padding: 60px 20px; }
.welcome-icon { color: rgba(6,182,212,0.2); margin-bottom: 16px; }
.ai-welcome h3 { font-size: 20px; color: var(--text-primary); margin-bottom: 8px; font-weight: 700; }
.ai-welcome p { font-size: 14px; color: var(--text-muted); margin-bottom: 24px; }

.suggestions { display: flex; flex-wrap: wrap; justify-content: center; gap: 8px; }

.suggestion-chip {
  padding: 8px 18px; border-radius: 20px;
  background: rgba(6, 182, 212, 0.08);
  border: 1px solid rgba(6, 182, 212, 0.15);
  color: var(--primary-light); font-size: 13px; cursor: pointer;
  transition: all var(--transition-fast);
}
.suggestion-chip:hover { background: rgba(6,182,212,0.15); border-color: rgba(6,182,212,0.3); transform: translateY(-1px); }

.ai-message { display: flex; gap: 12px; margin-bottom: 20px; }
.msg-avatar { flex-shrink: 0; }
.msg-body { flex: 1; min-width: 0; }
.msg-role { font-size: 12px; font-weight: 600; color: var(--text-muted); margin-bottom: 4px; }

.msg-content {
  font-size: 14px; line-height: 1.6; color: var(--text-primary);
  padding: 12px 16px; border-radius: 12px;
}
.user .msg-content { background: rgba(6,182,212,0.06); border-top-left-radius: 4px; }
.assistant .msg-content { background: #f0f2f5; border-top-right-radius: 4px; }

:deep(.msg-content pre) {
  background: #0a0e1a; color: #e2e8f0; padding: 12px 16px;
  border-radius: 8px; overflow-x: auto; font-size: 13px; margin: 8px 0;
  border: 1px solid var(--border-color);
}
:deep(.msg-content code) { background: rgba(148,163,184,0.1); padding: 2px 6px; border-radius: 4px; font-size: 13px; color: #22d3ee; }
:deep(.msg-content pre code) { background: transparent; padding: 0; color: inherit; }

.typing-dots { display: flex; gap: 5px; padding: 12px 16px; background: #f0f2f5; border-radius: 12px; border-top-right-radius: 4px; width: fit-content; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: var(--text-muted); animation: typing 1.4s infinite ease-in-out; }
.dot:nth-child(1) { animation-delay: 0s; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing { 0%,60%,100% { transform: translateY(0); opacity: 0.4; } 30% { transform: translateY(-8px); opacity: 1; } }

.ai-input-area { padding: 12px 24px; border-top: 1px solid var(--border-color); flex-shrink: 0; }
.ai-input { margin-bottom: 8px; }
:deep(.ai-input .el-textarea__inner) { border-radius: 10px; resize: none; background: var(--bg-secondary) !important; }
.input-footer { display: flex; justify-content: space-between; align-items: center; }
.input-hint { font-size: 12px; color: var(--text-muted); }
</style>
