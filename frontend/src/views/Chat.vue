<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- Sidebar -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <div class="sidebar-title">消息</div>
          <el-badge :value="totalRequests" :hidden="totalRequests === 0">
            <el-button :icon="Bell" circle size="small" @click="showRequests = true" text />
          </el-badge>
        </div>

        <div class="sidebar-tabs">
          <el-tabs v-model="activeTab" stretch @tab-change="onTabChange">
            <el-tab-pane label="好友" name="friends" />
            <el-tab-pane label="黑名单" name="blacklist" />
          </el-tabs>
        </div>

        <div class="search-box">
          <el-input v-model="searchKeyword" placeholder="搜索用户..." :prefix-icon="Search" size="small" clearable @keyup.enter="searchUsers" />
        </div>

        <div v-show="activeTab === 'friends'" class="friend-list">
          <div v-if="searchResults.length > 0" class="search-results">
            <div class="section-label">搜索结果</div>
            <div v-for="user in searchResults" :key="'sr-' + user.id" class="friend-item" @click="addFriend(user)">
              <el-avatar :size="40" :icon="UserFilled" />
              <div class="friend-info">
                <div class="friend-name">{{ user.nickname || user.username }}</div>
                <div class="friend-status">@{{ user.username }}</div>
              </div>
              <el-button size="small" type="primary" text :icon="Plus" />
            </div>
          </div>

          <div v-if="pendingRequests.length > 0" class="section-label">好友请求 ({{ pendingRequests.length }})</div>
          <div v-for="req in pendingRequests" :key="'req-' + req.id" class="friend-item request-item">
            <el-avatar :size="40" :icon="UserFilled" />
            <div class="friend-info">
              <div class="friend-name">{{ req.senderNickname || req.senderUsername }}</div>
              <div class="friend-status">请求添加好友</div>
            </div>
            <div class="request-actions">
              <el-button size="small" type="success" circle @click.stop="acceptRequest(req.id)">
                <el-icon><Check /></el-icon>
              </el-button>
              <el-button size="small" type="danger" circle @click.stop="rejectRequest(req.id)">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>

          <div v-if="chatStore.friends.length > 0" class="section-label">我的好友</div>
          <div v-for="friend in chatStore.friends" :key="'f-' + friend.friendId"
               class="friend-item" :class="{ active: currentChat?.friendId === friend.friendId }"
               @click="selectFriend(friend)">
            <div class="avatar-wrapper">
              <el-avatar :size="44" :icon="UserFilled" />
              <span class="online-dot" :class="{ online: friend.isOnline }"></span>
            </div>
            <div class="friend-info">
              <div class="friend-name">{{ friend.nickname || friend.username }}</div>
              <div class="friend-status">
                <span v-if="friend.isOnline" class="text-online">在线</span>
                <span v-else class="text-offline">{{ formatLastOnline(friend.lastOnlineTime) }}</span>
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

        <div v-show="activeTab === 'blacklist'" class="friend-list">
          <div v-for="item in chatStore.blacklist" :key="'bl-' + item.id" class="friend-item">
            <el-avatar :size="40" :icon="UserFilled" />
            <div class="friend-info">
              <div class="friend-name">{{ item.nickname || item.username }}</div>
              <div class="friend-status">已拉黑</div>
            </div>
            <el-button size="small" type="primary" text @click="unblockUser(item.blockedUserId)">解除拉黑</el-button>
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
                  <span v-if="currentChat.isOnline" class="text-online">在线</span>
                  <span v-else class="text-offline">{{ formatLastOnline(currentChat.lastOnlineTime) }}</span>
                </div>
              </div>
            </div>
            <div class="chat-actions">
              <el-tooltip content="删除好友"><el-button :icon="Delete" circle text @click="deleteFriendAction" /></el-tooltip>
              <el-tooltip content="拉黑"><el-button :icon="Minus" circle text @click="blockFriendAction" /></el-tooltip>
            </div>
          </div>

          <div class="messages-area" ref="messagesRef">
            <div v-for="(group, gIdx) in groupedMessages" :key="gIdx">
              <div class="time-divider">{{ gIdx }}</div>
              <div v-for="msg in group" :key="msg.id || msg.sendTime"
                   class="message-item"
                   :class="{ 'message-self': msg.senderId === currentUserId, 'message-other': msg.senderId !== currentUserId }">
                <div class="message-bubble-wrapper">
                  <div class="message-bubble">{{ msg.content }}</div>
                  <div class="message-meta">
                    <span class="message-time">{{ formatTime(msg.sendTime) }}</span>
                    <span v-if="msg.senderId === currentUserId" class="read-status">
                      <el-icon v-if="msg.isRead" color="#06b6d4" :size="12"><Check /></el-icon>
                      <el-icon v-else color="#64748b" :size="12"><Clock /></el-icon>
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="chat-input-area">
            <el-input v-model="messageText" type="textarea" :rows="3" placeholder="输入消息..." @keyup.enter.prevent="sendMessage" class="chat-input" />
            <div class="input-footer">
              <span class="input-hint">按 Enter 发送</span>
              <el-button type="primary" @click="sendMessage" :disabled="!messageText.trim()" round>发送</el-button>
            </div>
          </div>
        </template>
        <template v-else>
          <div class="no-chat">
            <div class="no-chat-icon"><el-icon :size="64"><ChatDotRound /></el-icon></div>
            <p>选择一个好友开始聊天</p>
          </div>
        </template>
      </div>
    </div>

    <el-dialog v-model="showRequests" title="好友请求" width="420px">
      <div v-for="req in chatStore.friendRequests" :key="'d-req-' + req.id" class="request-card">
        <div class="request-info">
          <el-avatar :size="40" :icon="UserFilled" />
          <div>
            <div class="request-name">{{ req.senderNickname || req.senderUsername }}</div>
            <div class="request-remark">{{ req.remark || '请求添加好友' }}</div>
          </div>
        </div>
        <div>
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
import { Search, Plus, Check, Close, MoreFilled, Bell, UserFilled, Delete, Minus, ChatDotRound, Clock } from '@element-plus/icons-vue'
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
const pendingRequests = computed(() => chatStore.friendRequests.filter(r => r.status === 'PENDING'))
const totalRequests = computed(() => pendingRequests.value.length)

const groupedMessages = computed(() => {
  const groups = {}
  chatStore.messages.forEach(msg => {
    const d = new Date(msg.sendTime)
    const key = d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
    if (!groups[key]) groups[key] = []
    groups[key].push(msg)
  })
  return groups
})

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t), n = new Date()
  const h = String(d.getHours()).padStart(2, '0'), m = String(d.getMinutes()).padStart(2, '0')
  if (d.toDateString() === n.toDateString()) return `${h}:${m}`
  const y = new Date(n); y.setDate(y.getDate() - 1)
  if (d.toDateString() === y.toDateString()) return `昨天 ${h}:${m}`
  if (d.getFullYear() === n.getFullYear()) return `${d.getMonth()+1}月${d.getDate()}日 ${h}:${m}`
  return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日 ${h}:${m}`
}

const formatLastOnline = (t) => {
  if (!t) return '离线'
  const diff = Math.floor((new Date() - new Date(t)) / 1000)
  if (diff < 60) return '刚刚在线'
  if (diff < 3600) return `${Math.floor(diff/60)}分钟前在线`
  if (diff < 86400) return `${Math.floor(diff/3600)}小时前在线`
  return `${Math.floor(diff/86400)}天前在线`
}

const selectFriend = async (friend) => {
  currentChat.value = friend
  try { await Promise.all([chatStore.fetchConversation(friend.friendId), chatStore.markAsRead(friend.friendId)]); chatStore.sendReadReceipt(friend.friendId) }
  catch (e) { console.warn('selectFriend error:', e) }
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
  const kw = searchKeyword.value.trim()
  if (!kw) { searchResults.value = []; return }
  try { const r = await axios.get(`/api/user/search?keyword=${kw}`); if (r.data.code === 200) searchResults.value = r.data.data.filter(u => u.id !== currentUserId.value && u.role !== 'ADMIN') }
  catch (e) { console.error('Search failed', e) }
}

const addFriend = async (user) => {
  try { await chatStore.sendFriendRequest(user.id, '你好，加个好友吧！'); ElMessage.success('好友请求已发送'); searchKeyword.value = ''; searchResults.value = [] }
  catch (e) { ElMessage.error(e.message || '请求失败') }
}

const acceptRequest = async (id) => { await chatStore.handleRequest(id, true); ElMessage.success('已同意好友请求') }
const rejectRequest = async (id) => { await chatStore.handleRequest(id, false); ElMessage.success('已拒绝好友请求') }

const handleFriendAction = (cmd, friend) => { if (cmd === 'delete') deleteFriendAction(friend); else if (cmd === 'block') blockFriendAction(friend) }

const deleteFriendAction = async (friend) => {
  const t = friend || currentChat.value; if (!t) return
  try { await ElMessageBox.confirm(`确定要删除好友"${t.nickname||t.username}"吗？`, '确认删除'); await chatStore.deleteFriend(t.friendId); if (currentChat.value?.friendId === t.friendId) currentChat.value = null; ElMessage.success('好友已删除') }
  catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const blockFriendAction = async (friend) => {
  const t = friend || currentChat.value; if (!t) return
  try { await ElMessageBox.confirm(`确定要拉黑"${t.nickname||t.username}"吗？`, '确认拉黑'); await chatStore.blockUser(t.friendId); if (currentChat.value?.friendId === t.friendId) currentChat.value = null; ElMessage.success('已拉黑') }
  catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const unblockUser = async (id) => { try { await chatStore.unblockUser(id); ElMessage.success('已解除拉黑') } catch (e) { ElMessage.error('操作失败') } }

const scrollToBottom = () => { nextTick(() => { if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight }) }

const onTabChange = () => { if (activeTab.value === 'blacklist') chatStore.fetchBlacklist() }

watch(() => chatStore.messages, () => scrollToBottom(), { deep: true })

onMounted(() => { chatStore.fetchFriends(); chatStore.fetchRequests(); chatStore.fetchUnreadCounts() })
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 140px); max-width: 1400px; margin: 0 auto;
  animation: fadeSlideUp 0.5s ease-out;
}
@keyframes fadeSlideUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }

.chat-container {
  height: 100%; display: flex;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

/* ===== Sidebar ===== */
.chat-sidebar {
  width: 320px; min-width: 320px;
  border-right: 1px solid var(--border-color);
  display: flex; flex-direction: column;
}

.sidebar-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px 8px; }
.sidebar-title { font-size: 17px; font-weight: 700; color: var(--text-primary); }
.sidebar-tabs { padding: 0 16px; }
.search-box { padding: 8px 16px; }

.friend-list { flex: 1; overflow-y: auto; padding: 0 10px 10px; }

.section-label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-muted); padding: 12px 10px 6px; }

.friend-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 12px; border-radius: 10px; cursor: pointer;
  transition: all var(--transition-fast); margin-bottom: 2px;
}
.friend-item:hover { background: rgba(255,255,255,0.03); }
.friend-item.active { background: rgba(6,182,212,0.08); }

.friend-info { flex: 1; min-width: 0; }
.friend-name { font-size: 14px; font-weight: 600; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.friend-status { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
.friend-meta { display: flex; align-items: center; gap: 4px; }

.avatar-wrapper { position: relative; }

.online-dot {
  position: absolute; bottom: 2px; right: 2px;
  width: 10px; height: 10px; border-radius: 50%;
  background: #64748b; border: 2px solid var(--bg-primary);
}
.online-dot.online { background: var(--success-color); }

.request-item { background: rgba(245,158,11,0.06); margin-bottom: 4px; border-radius: 10px; }
.request-actions { display: flex; gap: 4px; }

.text-online { color: var(--success-color); font-size: 12px; }
.text-offline { color: var(--text-muted); font-size: 12px; }

/* ===== Chat Main ===== */
.chat-main { flex: 1; display: flex; flex-direction: column; min-width: 0; }

.chat-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 24px; border-bottom: 1px solid var(--border-color);
  background: var(--bg-secondary);
}
.chat-user-info { display: flex; align-items: center; gap: 12px; }
.chat-user-name { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.chat-user-status { font-size: 12px; }
.chat-actions { display: flex; gap: 4px; }

.messages-area {
  flex: 1; overflow-y: auto; padding: 20px 24px;
  background: var(--bg-tertiary);
}

.time-divider {
  text-align: center; color: var(--text-muted);
  font-size: 12px; padding: 12px 0; position: relative;
}
.time-divider::before, .time-divider::after {
  content: ''; position: absolute; top: 50%;
  width: 25%; height: 1px; background: var(--border-color);
}
.time-divider::before { left: 8%; }
.time-divider::after { right: 8%; }

.message-item { display: flex; margin-bottom: 16px; }
.message-self { justify-content: flex-end; }
.message-other { justify-content: flex-start; }

.message-bubble-wrapper { max-width: 60%; }

.message-bubble {
  padding: 10px 16px; border-radius: 14px;
  font-size: 14px; line-height: 1.5; word-break: break-word;
}
.message-self .message-bubble {
  background: linear-gradient(135deg, #06b6d4, #0891b2);
  color: #fff; border-bottom-right-radius: 4px;
}
.message-other .message-bubble {
  background: #f0f2f5;
  color: var(--text-primary); border-bottom-left-radius: 4px;
}

.message-meta { display: flex; align-items: center; gap: 4px; margin-top: 4px; padding: 0 4px; }
.message-time { font-size: 11px; color: var(--text-muted); }
.read-status { display: flex; align-items: center; }

.chat-input-area {
  padding: 12px 24px; border-top: 1px solid var(--border-color);
  background: var(--bg-secondary);
}
.chat-input { margin-bottom: 8px; }
:deep(.chat-input .el-textarea__inner) { border-radius: 10px; resize: none; background: var(--bg-secondary) !important; }

.input-footer { display: flex; justify-content: space-between; align-items: center; }
.input-hint { font-size: 12px; color: var(--text-muted); }

.no-chat {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 16px;
}
.no-chat-icon { color: rgba(148,163,184,0.2); }
.no-chat p { font-size: 15px; color: var(--text-muted); }

/* ===== Request Dialog ===== */
.request-card {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 0; border-bottom: 1px solid var(--border-light);
}
.request-info { display: flex; align-items: center; gap: 12px; }
.request-name { font-weight: 500; color: var(--text-primary); font-size: 14px; }
.request-remark { font-size: 12px; color: var(--text-muted); }
</style>
