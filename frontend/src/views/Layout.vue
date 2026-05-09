<template>
  <div class="layout-container">
    <el-container class="main-container">
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
        <div class="sidebar-header">
          <div class="logo" v-show="!isCollapse">
            <el-icon :size="28" color="#409eff"><Monitor /></el-icon>
            <span class="logo-text">设备管理</span>
          </div>
          <div class="logo-mini" v-show="isCollapse">
            <el-icon :size="24" color="#409eff"><Monitor /></el-icon>
          </div>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>

          <el-sub-menu index="admin" v-if="authStore.isAdminUser">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>管理</span>
            </template>
            <el-menu-item index="/devices">
              <el-icon><Monitor /></el-icon>
              <span>设备管理</span>
            </el-menu-item>
            <el-menu-item index="/users">
              <el-icon><UserFilled /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>在线聊天</span>
          </el-menu-item>

          <el-menu-item index="/ai">
            <el-icon><MagicStick /></el-icon>
            <span>AI 助手</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="20">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-badge :value="totalUnread" :hidden="totalUnread === 0" class="header-badge">
              <el-icon :size="20" @click="$router.push('/chat')" style="cursor:pointer">
                <ChatDotRound />
              </el-icon>
            </el-badge>
            <el-dropdown trigger="click">
              <div class="user-info">
                <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
                <span class="username">{{ authStore.currentUser?.nickname || authStore.currentUser?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">
                    <el-icon><User /></el-icon>个人信息
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/ai')">
                    <el-icon><MagicStick /></el-icon>AI 助手
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useChatStore } from '../store/chat'
import {
  Monitor, Odometer, User, Setting, ChatDotRound, MagicStick,
  Fold, Expand, UserFilled, ArrowDown, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const chatStore = useChatStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const totalUnread = computed(() => {
  return Object.values(chatStore.unreadCounts).reduce((a, b) => a + b, 0)
})

const handleLogout = () => {
  chatStore.disconnectWebSocket()
  authStore.logout()
}

onMounted(() => {
  if (authStore.isLoggedIn && authStore.currentUser) {
    chatStore.connectWebSocket(authStore.currentUser.id)
    chatStore.fetchFriends()
    chatStore.fetchUnreadCounts()
  }

  // 页面切换回前台时刷新未读计数
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

const handleVisibilityChange = () => {
  if (!document.hidden && authStore.isLoggedIn) {
    chatStore.fetchUnreadCounts()
  }
}

onUnmounted(() => {
  chatStore.disconnectWebSocket()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.main-container {
  height: 100%;
}

.sidebar {
  background: var(--sidebar-bg);
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
}

.logo-mini {
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 60px);
  overflow-y: auto;
}

.header {
  background: var(--header-bg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: var(--primary-color);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-badge :deep(.el-badge__content) {
  background: #f56c6c;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 8px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

.user-avatar {
  background: var(--primary-color);
}

.main-content {
  background: var(--bg-color);
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 60px);
}
</style>
