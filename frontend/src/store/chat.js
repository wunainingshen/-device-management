import { defineStore } from 'pinia'
import axios from '../utils/axios'

export const useChatStore = defineStore('chat', {
  state: () => ({
    friends: [],
    friendRequests: [],
    blacklist: [],
    currentChat: null,
    messages: [],
    unreadCounts: {},
    ws: null,
    connected: false
  }),

  actions: {
    async fetchFriends() {
      const res = await axios.get('/api/friend/list')
      if (res.data.code === 200) {
        this.friends = res.data.data
      }
    },

    async fetchRequests() {
      const res = await axios.get('/api/friend/requests')
      if (res.data.code === 200) {
        this.friendRequests = res.data.data
      }
    },

    async fetchBlacklist() {
      const res = await axios.get('/api/friend/blacklist')
      if (res.data.code === 200) {
        this.blacklist = res.data.data
      }
    },

    async sendFriendRequest(receiverId, remark) {
      const res = await axios.post('/api/friend/request', { receiverId, remark })
      if (res.data.code !== 200) {
        throw new Error(res.data.message)
      }
    },

    async handleRequest(requestId, accept) {
      await axios.post(`/api/friend/request/${requestId}/handle?accept=${accept}`)
      await this.fetchRequests()
      if (accept) await this.fetchFriends()
    },

    async deleteFriend(friendId) {
      await axios.delete(`/api/friend/${friendId}`)
      await this.fetchFriends()
    },

    async blockUser(blockedUserId) {
      await axios.post(`/api/friend/block/${blockedUserId}`)
      await this.fetchFriends()
      await this.fetchBlacklist()
    },

    async unblockUser(blockedUserId) {
      await axios.post(`/api/friend/unblock/${blockedUserId}`)
      await Promise.all([
        this.fetchBlacklist(),
        this.fetchFriends()
      ])
    },

    async fetchConversation(friendId) {
      const res = await axios.get(`/api/chat/conversation/${friendId}`)
      if (res.data.code === 200) {
        this.messages = res.data.data
      }
    },

    async markAsRead(friendId) {
      try {
        await axios.post(`/api/chat/read/${friendId}`)
      } catch (e) {
        // 即使API失败也清除前端未读计数
        console.warn('markAsRead API failed, clearing local count anyway', e)
      }
      this.unreadCounts[friendId] = 0
    },

    async fetchUnreadCounts() {
      const res = await axios.get('/api/chat/unread-counts')
      if (res.data.code === 200) {
        this.unreadCounts = res.data.data
      }
    },

    connectWebSocket(userId) {
      if (this.ws && this.connected) return

      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const wsUrl = `${protocol}//${window.location.host}/ws/chat?userId=${userId}`

      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        this.connected = true
        console.log('WebSocket connected')
      }

      this.ws.onmessage = event => {
        const data = JSON.parse(event.data)
        this.handleWsMessage(data)
      }

      this.ws.onclose = () => {
        this.connected = false
        console.log('WebSocket disconnected')
        setTimeout(() => this.connectWebSocket(userId), 3000)
      }

      this.ws.onerror = error => {
        console.error('WebSocket error:', error)
      }
    },

    handleWsMessage(data) {
      switch (data.type) {
        case 'CHAT':
          if (data.senderId === this.getCurrentUserId()) {
            // 自己发的消息（通过其他设备/窗口），跳过
            return
          }
          this.messages.push(data)
          if (this.currentChat && data.senderId === this.currentChat.friendId) {
            this.unreadCounts[data.senderId] = 0  // 立即清零，不等异步
            this.markAsRead(this.currentChat.friendId)
            this.sendReadReceipt(this.currentChat.friendId)
          } else {
            this.unreadCounts[data.senderId] = (this.unreadCounts[data.senderId] || 0) + 1
          }
          break
        case 'CHAT_ACK':
          // 替换乐观更新的临时消息为服务端确认的消息
          const ackIdx = this.messages.findIndex(m => m._temp === true)
          if (ackIdx !== -1) {
            this.messages[ackIdx] = { ...data, isRead: data.isRead || false }
          } else {
            this.messages.push(data)
          }
          break
        case 'MESSAGE_READ':
          // readBy 表示谁已读，标记发送给该好友的消息为已读
          this.messages.forEach(m => {
            if (m.senderId === this.getCurrentUserId() && m.receiverId === data.readBy) {
              m.isRead = true
            }
          })
          // 更新未读计数
          if (this.unreadCounts[data.readBy]) {
            this.unreadCounts[data.readBy] = 0
          }
          break
        case 'USER_STATUS':
          const friend = this.friends.find(f => f.friendId === data.userId)
          if (friend) {
            friend.isOnline = data.online
            friend.lastOnlineTime = data.online ? null : new Date().toISOString()
          }
          break
        case 'TYPING':
          // Handle typing indicator
          break
        case 'ERROR':
          // Handle error (blocked message)
          break
      }
    },

    getCurrentUserId() {
      const user = JSON.parse(localStorage.getItem('user') || 'null')
      return user?.id
    },

    sendMessage(receiverId, content) {
      if (this.ws && this.connected) {
        const clientId = 'c' + Date.now() + '-' + Math.random().toString(36).substr(2, 6)
        // 乐观更新：立即在本地显示消息
        const tempMsg = {
          id: clientId,
          _clientId: clientId,
          senderId: this.getCurrentUserId(),
          receiverId: receiverId,
          content: content,
          sendTime: new Date().toISOString(),
          isRead: false,
          _temp: true
        }
        this.messages.push(tempMsg)

        this.ws.send(JSON.stringify({
          type: 'CHAT',
          _clientId: clientId,
          receiverId,
          content
        }))
      }
    },

    sendTyping(receiverId, isTyping) {
      if (this.ws && this.connected) {
        this.ws.send(JSON.stringify({
          type: 'TYPING',
          receiverId,
          isTyping
        }))
      }
    },

    sendReadReceipt(friendId) {
      if (this.ws && this.connected) {
        this.ws.send(JSON.stringify({
          type: 'MARK_READ',
          friendId
        }))
      }
    },

    disconnectWebSocket() {
      if (this.ws) {
        this.ws.close()
        this.ws = null
        this.connected = false
      }
    }
  }
})
