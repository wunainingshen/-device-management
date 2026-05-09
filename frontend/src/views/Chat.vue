<template>
  <div class="chat-page">
    <el-card class="chat-container" shadow="hover" :body-style="{ padding: 0 }">
      <div class="chat-layout">
        <!-- Sidebar -->
        <div class="chat-sidebar">
          <div class="sidebar-title">
            <span>消息</span>
            <el-badge :value="totalRequests" :hidden="totalRequests === 0">
              <el-icon :size="20" @click="showRequests = true" style="cursor:pointer">
                <Bell />
              </el-icon>
            </el-badge>
          </div>

          <!-- Tabs -->
          <div class="friend-tabs">
            <el-tabs v-model="activeTab" stretch @tab-change="onTabChange">
              <el-tab-pane label="好友" name="friends" />
              <el-tab-pane label="黑名单" name="blacklist" />
            </el-tabs>
          </div>

          <!-- Search -->
          <div class="search-box">
            <el-input v-model="searchKeyword" placeholder="搜索用户..." :prefix-icon="Search" size="small" clearable @keyup.enter="searchUsers" />
          </div>

          <!-- Friend List -->
          <div v-show="activeTab === 'friends'" class="friend-list">
            <div v-if="searchResults.length > 0" class="search-results">
              <div class="section-label">搜索结果</div>
              <div
                v-for="user in searchResults"
                :key="'sr-' + user.id"
                class="friend-item"
                @click="addFriend(user)"
              >
                <el-avatar :size="40" :icon="UserFilled" />
                <div class="friend-info">
                  <div class="friend-name">{{ user.nickname || user.username }}</div>
                  <div class="friend-username">@{{ user.username }}</div>
                </div>
                <el-button size="small" type="primary" text>
                  <el-icon><Plus /></el-icon>添加
                </el-button>
              </div>
            </div>

            <div class="section-label" v-if="chatStore.friendRequests.filter(r => r.status === 'PENDING').length > 0">
              好友请求
            </div>
            <div
              v-for="req in pendingRequests"
              :key="'req-' + req.id"
              class="friend-item request-item"
            >
              <el-avatar :size="40" :icon="UserFilled" />
              <div class="friend-info">
                <div class="friend-name">{{ req.senderNickname || req.senderUsername }}</div>
                <div class="friend-username">请求添加好友</div>
              </div>
              <div class="request-actions">
                <el-button size="small" type="success" circle @click="acceptRequest(req.id)">
                  <el-icon><Check /></el-icon>
                </el-button>
                <el-button size="small" type="danger" circle @click="rejectRequest(req.id)">
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
            </div>

            <div class="section-label" v-if="chatStore.friends.length > 0">我的好友</div>
            <div
              v-for="friend in chatStore.friends"
              :key="'f-' + friend.friendId"
              class="friend-item"
              :class="{ active: currentChat?.friendId === friend.friendId }"
              @click="selectFriend(friend)"
            >
              <div class="avatar-wrapper">
                <el-avatar :size="44" :icon="UserFilled" />
                <span class="online-dot" :class="{ online: friend.isOnline }"></span>
              </div>
              <div class="friend-info">
                <div class="friend-name">{{ friend.nickname || friend.username }}</div>
                <div class="friend-status">
                  <span v-if="friend.isOnline" class="online-text">在线</span>
                  <span v-else class="offline-text">{{ formatLastOnline(friend.lastOnlineTime) }}</span>
                </div>
              </div>
              <div class="friend-meta">
                <el-badge :value="chatStore.unreadCounts[friend.friendId] || 0" :hidden="!chatStore.unreadCounts[friend.friendId]" class="unread-badge" />
                <el-dropdown trigger="click" @command="(cmd) => handleFriendAction(cmd, friend)">
                  <el-button size="small" text :icon="MoreFilled" circle />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="delete"><el-icon><Delete /></el-icon>删除好友</el-dropdown-item>
                      <el-dropdown-item command="block"><el-icon><Minus /></el-icon>拉黑好友</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <el-empty v-if="chatStore.friends.length === 0 && pendingRequests.length === 0 && searchResults.length === 0" description="暂无好友，搜索添加吧" />
          </div>

          <!-- Blacklist -->
          <div v-show="activeTab === 'blacklist'" class="friend-list">
            <div
              v-for="item in chatStore.blacklist"
              :key="'bl-' + item.id"
              class="friend-item"
            >
              <el-avatar :size="40" :icon="UserFilled" />
              <div class="friend-info">
                <div class="friend-name">{{ item.nickname || item.username }}</div>
                <div class="friend-username">已拉黑</div>
              </div>
              <el-button size="small" type="primary" text @click="unblockUser(item.blockedUserId)">
                解除拉黑
              </el-button>
            </div>
            <el-empty v-if="chatStore.blacklist.length === 0" description="黑名单为空" />
          </div>
        </div>

        <!-- Chat Area -->
        <div class="chat-main">
          <template v-if="currentChat">
            <div class="chat-header">
              <div class="chat-user-info">
                <el-avatar :size="40" :icon="UserFilled" />
                <div>
                  <div class="chat-user-name">{{ currentChat.nickname || currentChat.username }}</div>
                  <div class="chat-user-status">
                    <span v-if="currentChat.isOnline" class="online-text">在线</span>
                    <span v-else class="offline-text">{{ formatLastOnline(currentChat.lastOnlineTime) }}</span>
                  </div>
                </div>
              </div>
              <div class="chat-header-actions">
                <el-button :icon="Delete" circle text @click="deleteFriendAction" />
                <el-button :icon="Minus" circle text @click="blockFriendAction" />
              </div>
            </div>

            <div class="messages-area" ref="messagesRef">
              <div v-for="(group, gIdx) in groupedMessages" :key="gIdx">
                <div class="time-divider">{{ gIdx }}</div>
                <div
                  v-for="msg in group"
                  :key="msg.id || msg.sendTime"
                  class="message-item"
                  :class="{ 'message-self': msg.senderId === currentUserId, 'message-other': msg.senderId !== currentUserId }"
                >
                  <div class="message-content">
                    <div class="message-bubble">{{ msg.content }}</div>
                    <div class="message-meta">
                      <span class="message-time">{{ formatTime(msg.sendTime) }}</span>
                      <span v-if="msg.senderId === currentUserId" class="read-status">
                        <el-icon v-if="msg.isRead" color="#409eff" :size="12"><Check /></el-icon>
                        <el-icon v-else color="#909399" :size="12"><Clock /></el-icon>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="chat-input-area">
              <el-input
                v-model="messageText"
                type="textarea"
                :rows="3"
                placeholder="输入消息..."
                @keyup.enter.prevent="sendMessage"
                class="chat-input"
              />
              <div class="input-actions">
                <span class="input-tip">按 Enter 发送</span>
                <el-button type="primary" @click="sendMessage" :disabled="!messageText.trim()">
                  发送
                </el-button>
              </div>
            </div>
          </template>

          <template v-else>
            <div class="no-chat">
              <el-icon :size="80" color="#dcdfe6"><ChatDotRound /></el-icon>
              <p>选择一个好友开始聊天</p>
            </div>
          </template>
        </div>
      </div>
    </el-card>

    <!-- Friend Requests Dialog -->
    <el-dialog v-model="showRequests" title="好友请求" width="400px">
      <div v-for="req in chatStore.friendRequests" :key="'d-req-' + req.id" class="request-card">
        <div class="request-info">
          <el-avatar :size="40" :icon="UserFilled" />
          <div>
            <div class="request-name">{{ req.senderNickname || req.senderUsername }}</div>
            <div class="request-remark">{{ req.remark || '请求添加好友' }}</div>
          </div>
        </div>
        <div class="request-status">
          <el-tag v-if="req.status === 'PENDING'" type="warning">待处理</el-tag>
          <el-tag v-else-if="req.status === 'ACCEPTED'" type="success">已接受</el-tag>
          <el-tag v-else type="info">已拒绝</el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useChatStore } from '../store/chat'
import {
  Search, Plus, Check, Close, MoreFilled, Bell,
  UserFilled, Delete, Minus, ChatDotRound, Clock
} from '@element-plus/icons-vue'
import axios from '../utils/axios'

const chatStore = useChatStore()
const messagesRef = ref(null)
const messageText = ref('')
const currentChat = ref(null)
const searchKeyword = ref('')
const searchResults = ref([])
const activeTab = ref('friends')
const showRequests = ref(false)

const currentUserId = computed(() => chatStore.getCurrentUserId())

const pendingRequests = computed(() =>
  chatStore.friendRequests.filter(r => r.status === 'PENDING')
)

const totalRequests = computed(() => pendingRequests.value.length)

const groupedMessages = computed(() => {
  const groups = {}
  chatStore.messages.forEach(msg => {
    const date = new Date(msg.sendTime).toLocaleDateString('zh-CN', {
      year: 'numeric', month: 'long', day: 'numeric'
    })
    if (!groups[date]) groups[date] = []
    groups[date].push(msg)
  })
  return groups
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')

  // Same day: show time only
  if (date.toDateString() === now.toDateString()) {
    return `${hours}:${minutes}`
  }

  // Yesterday
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${hours}:${minutes}`
  }

  // This year
  if (date.getFullYear() === now.getFullYear()) {
    return `${date.getMonth() + 1}月${date.getDate()}日 ${hours}:${minutes}`
  }

  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${hours}:${minutes}`
}

const formatLastOnline = (time) => {
  if (!time) return '离线'
  const last = new Date(time)
  const now = new Date()
  const diff = Math.floor((now - last) / 1000)

  if (diff < 60) return '刚刚在线'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前在线`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前在线`
  return `${Math.floor(diff / 86400)}天前在线`
}

const selectFriend = async (friend) => {
  currentChat.value = friend
  try {
    await Promise.all([
      chatStore.fetchConversation(friend.friendId),
      chatStore.markAsRead(friend.friendId)
    ])
    chatStore.sendReadReceipt(friend.friendId)
  } catch (e) {
    console.warn('selectFriend error:', e)
  }
  nextTick(() => scrollToBottom())
}

const sendMessage = () => {
  const text = messageText.value.trim()
  if (!text || !currentChat.value) return

  chatStore.sendMessage(currentChat.value.friendId, text)
  messageText.value = ''
  nextTick(() => scrollToBottom())
}

const searchUsers = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResults.value = []
    return
  }
  try {
    const res = await axios.get(`/api/user/search?keyword=${keyword}`)
    if (res.data.code === 200) {
      searchResults.value = res.data.data.filter(u => u.id !== currentUserId.value && u.role !== 'ADMIN')
    }
  } catch (e) {
    console.error('Search failed', e)
  }
}

const addFriend = async (user) => {
  try {
    await chatStore.sendFriendRequest(user.id, '你好，加个好友吧！')
    ElMessage.success('好友请求已发送')
    searchKeyword.value = ''
    searchResults.value = []
  } catch (e) {
    ElMessage.error(e.message || '请求失败')
  }
}

const acceptRequest = async (requestId) => {
  await chatStore.handleRequest(requestId, true)
  ElMessage.success('已同意好友请求')
}

const rejectRequest = async (requestId) => {
  await chatStore.handleRequest(requestId, false)
  ElMessage.success('已拒绝好友请求')
}

const handleFriendAction = (cmd, friend) => {
  if (cmd === 'delete') {
    deleteFriendAction(friend)
  } else if (cmd === 'block') {
    blockFriendAction(friend)
  }
}

const deleteFriendAction = async (friend) => {
  const target = friend || currentChat.value
  if (!target) return
  try {
    await ElMessageBox.confirm(`确定要删除好友"${target.nickname || target.username}"吗？`, '确认删除')
    await chatStore.deleteFriend(target.friendId)
    if (currentChat.value?.friendId === target.friendId) {
      currentChat.value = null
    }
    ElMessage.success('好友已删除')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const blockFriendAction = async (friend) => {
  const target = friend || currentChat.value
  if (!target) return
  try {
    await ElMessageBox.confirm(`确定要拉黑"${target.nickname || target.username}"吗？拉黑后将解除好友关系。`, '确认拉黑')
    await chatStore.blockUser(target.friendId)
    if (currentChat.value?.friendId === target.friendId) {
      currentChat.value = null
    }
    ElMessage.success('已拉黑')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const unblockUser = async (userId) => {
  try {
    await chatStore.unblockUser(userId)
    ElMessage.success('已解除拉黑')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const onTabChange = () => {
  if (activeTab.value === 'blacklist') {
    chatStore.fetchBlacklist()
  }
}

watch(() => chatStore.messages, () => {
  scrollToBottom()
}, { deep: true })

onMounted(() => {
  chatStore.fetchFriends()
  chatStore.fetchRequests()
  chatStore.fetchUnreadCounts()
})

onUnmounted(() => {
  // Don't disconnect websocket on route change - handled in Layout
})
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 140px);
  max-width: 1400px;
  margin: 0 auto;
}

.chat-container {
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.chat-layout {
  display: flex;
  height: 100%;
}

/* Sidebar */
.chat-sidebar {
  width: 320px;
  min-width: 320px;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 8px;
  font-size: 18px;
  font-weight: 600;
}

.friend-tabs {
  padding: 0 15px;
}

.search-box {
  padding: 8px 15px;
}

.friend-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 10px 10px;
}

.section-label {
  font-size: 12px;
  color: #909399;
  padding: 10px 10px 5px;
}

.friend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 2px;
}

.friend-item:hover {
  background: #f5f7fa;
}

.friend-item.active {
  background: #ecf5ff;
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.friend-username {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.friend-status {
  font-size: 12px;
  margin-top: 2px;
}

.online-text { color: #67c23a; }
.offline-text { color: #909399; font-size: 12px; }

.friend-meta {
  display: flex;
  align-items: center;
  gap: 5px;
}

.avatar-wrapper {
  position: relative;
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #dcdfe6;
  border: 2px solid #fff;
}

.online-dot.online {
  background: #67c23a;
}

.request-item {
  background: #fff8e1;
  margin-bottom: 4px;
  border-radius: 10px;
}

.request-actions {
  display: flex;
  gap: 5px;
}

.unread-badge :deep(.el-badge__content) {
  background: #f56c6c;
}

/* Chat Main */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-user-name {
  font-size: 16px;
  font-weight: 600;
}

.chat-user-status {
  font-size: 12px;
}

.chat-header-actions {
  display: flex;
  gap: 5px;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
}

.time-divider {
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding: 10px 0;
  position: relative;
}

.time-divider::before,
.time-divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 30%;
  height: 1px;
  background: #e0e0e0;
}

.time-divider::before { left: 5%; }
.time-divider::after { right: 5%; }

.message-item {
  display: flex;
  margin-bottom: 16px;
}

.message-self {
  justify-content: flex-end;
}

.message-other {
  justify-content: flex-start;
}

.message-content {
  max-width: 60%;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message-self .message-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-other .message-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #e8e8e8;
  border-bottom-left-radius: 4px;
}

.message-meta {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 4px;
  padding: 0 4px;
}

.message-time {
  font-size: 11px;
  color: #909399;
}

.read-status {
  display: flex;
  align-items: center;
}

.chat-input-area {
  padding: 12px 20px;
  border-top: 1px solid #e8e8e8;
  background: #fff;
}

.chat-input {
  margin-bottom: 8px;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 10px;
  resize: none;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.input-tip {
  font-size: 12px;
  color: #909399;
}

.no-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 15px;
}

.no-chat p {
  font-size: 16px;
}

/* Request dialog */
.request-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.request-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.request-name {
  font-weight: 500;
}

.request-remark {
  font-size: 12px;
  color: #909399;
}
</style>
