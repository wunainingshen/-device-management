<template>
  <div class="ai-page">
    <el-card class="ai-container" shadow="hover">
      <template #header>
        <div class="ai-header">
          <el-icon :size="22" color="#409eff"><MagicStick /></el-icon>
          <span>AI 智能助手</span>
          <el-tag type="info" effect="plain" size="small">DeepSeek</el-tag>
        </div>
      </template>

      <div class="ai-messages" ref="messagesRef">
        <div class="ai-welcome" v-if="messages.length === 0">
          <el-icon :size="60" color="#409eff"><MagicStick /></el-icon>
          <h3>你好！我是 AI 智能助手</h3>
          <p>我可以帮助你解答设备管理相关的问题，或者进行日常对话</p>
          <div class="suggestions">
            <el-tag
              v-for="(s, i) in suggestions"
              :key="i"
              class="suggestion-tag"
              effect="plain"
              @click="sendQuestion(s)"
            >
              {{ s }}
            </el-tag>
          </div>
        </div>

        <div v-for="(msg, idx) in messages" :key="idx" class="ai-message" :class="msg.role">
          <div class="message-avatar">
            <el-avatar v-if="msg.role === 'assistant'" :size="36" style="background: #409eff">
              <el-icon><MagicStick /></el-icon>
            </el-avatar>
            <el-avatar v-else :size="36" style="background: #67c23a">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <div class="message-body">
            <div class="message-role">{{ msg.role === 'assistant' ? 'AI 助手' : '你' }}</div>
            <div class="message-content markdown-body" v-html="renderContent(msg.content)"></div>
          </div>
        </div>

        <div v-if="loading" class="ai-message assistant">
          <div class="message-avatar">
            <el-avatar :size="36" style="background: #409eff"><el-icon><MagicStick /></el-icon></el-avatar>
          </div>
          <div class="message-body">
            <div class="message-role">AI 助手</div>
            <div class="typing-indicator">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>
      </div>

      <div class="ai-input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="输入您的问题..."
          @keyup.enter.prevent="sendMessage"
          :disabled="loading"
          class="ai-input"
        />
        <div class="input-footer">
          <span class="input-hint">按 Enter 发送</span>
          <el-button type="primary" @click="sendMessage" :loading="loading" :disabled="!inputText.trim()" round>
            发送
          </el-button>
        </div>
      </div>
    </el-card>
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

const suggestions = [
  '如何管理设备？',
  '帮我分析设备维护建议',
  '什么是好的设备管理实践？',
  '介绍一下设备管理系统的功能'
]

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  history.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  loading.value = true
  try {
    const res = await axios.post('/api/ai/chat', {
      message: text,
      history: history.value.slice(-20) // Keep last 20 messages for context
    })

    if (res.data.code === 200) {
      const reply = res.data.data
      messages.value.push({ role: 'assistant', content: reply })
      history.value.push({ role: 'assistant', content: reply })
    } else {
      throw new Error(res.data.message)
    }
  } catch (e) {
    ElMessage.error(e.message || 'AI服务暂时不可用')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const sendQuestion = (question) => {
  inputText.value = question
  sendMessage()
}

const renderContent = (content) => {
  if (!content) return ''
  // Simple markdown-like rendering
  let html = content
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')
  return html
}

watch(messages, () => scrollToBottom(), { deep: true })
</script>

<style scoped>
.ai-page {
  max-width: 900px;
  margin: 0 auto;
  height: calc(100vh - 140px);
}

.ai-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
}

:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 10px;
}

.ai-welcome {
  text-align: center;
  padding: 60px 20px;
  color: #606266;
}

.ai-welcome h3 {
  margin: 15px 0 8px;
  font-size: 20px;
  color: #303133;
}

.ai-welcome p {
  font-size: 14px;
  margin-bottom: 25px;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.suggestion-tag {
  cursor: pointer;
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 20px;
  transition: all 0.3s;
}

.suggestion-tag:hover {
  color: #409eff;
  border-color: #409eff;
  background: #ecf5ff;
}

.ai-message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-avatar {
  flex-shrink: 0;
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-role {
  font-size: 13px;
  font-weight: 500;
  color: #909399;
  margin-bottom: 5px;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  padding: 12px 16px;
  border-radius: 12px;
}

.user .message-content {
  background: #ecf5ff;
  border-top-left-radius: 4px;
}

.assistant .message-content {
  background: #f5f7fa;
  border-top-right-radius: 4px;
}

:deep(.message-content pre) {
  background: #282c34;
  color: #abb2bf;
  padding: 12px 16px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 13px;
  margin: 8px 0;
}

:deep(.message-content code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #e96900;
}

:deep(.message-content pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
}

.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 12px;
  border-top-right-radius: 4px;
  width: fit-content;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
  animation: typing 1.4s infinite ease-in-out;
}

.dot:nth-child(1) { animation-delay: 0s; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-8px); opacity: 1; }
}

.ai-input-area {
  padding-top: 12px;
  border-top: 1px solid #e8e8e8;
}

.ai-input {
  margin-bottom: 8px;
}

:deep(.ai-input .el-textarea__inner) {
  border-radius: 10px;
  resize: none;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.input-hint {
  font-size: 12px;
  color: #909399;
}
</style>
