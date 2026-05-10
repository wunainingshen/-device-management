<template>
  <div class="layout-container">
    <div class="layout-glow"></div>
    <el-container class="main-container">
      <el-aside :width="isCollapse ? '68px' : '240px'" class="sidebar">
        <div class="sidebar-header">
          <div class="logo" v-show="!isCollapse">
            <div class="logo-icon">
              <el-icon :size="24"><Monitor /></el-icon>
            </div>
            <span class="logo-text">DeviceFlow</span>
          </div>
          <div class="logo-mini" v-show="isCollapse">
            <div class="logo-icon">
              <el-icon :size="22"><Monitor /></el-icon>
            </div>
          </div>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="sidebar-menu"
        >
          <div class="menu-label" v-show="!isCollapse">导航</div>
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>

          <div class="menu-label" v-show="!isCollapse" v-if="authStore.isAdminUser" style="margin-top:8px">管理</div>
          <el-sub-menu index="admin" v-if="authStore.isAdminUser">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>管理控制台</span>
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

          <div class="menu-label" v-show="!isCollapse" style="margin-top:8px">工具</div>
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
            <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="18">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
          <div class="header-right">
            <el-badge :value="totalUnread" :hidden="totalUnread === 0" class="header-badge">
              <el-icon :size="20" @click="$router.push('/chat')" style="cursor:pointer">
                <ChatDotRound />
              </el-icon>
            </el-badge>
            <div class="header-divider"></div>
            <el-dropdown trigger="click">
              <div class="user-info">
                <el-avatar :size="34" :icon="UserFilled" class="user-avatar" />
                <div class="user-text">
                  <span class="username">{{ authStore.currentUser?.nickname || authStore.currentUser?.username }}</span>
                  <span class="user-role">{{ authStore.isAdminUser ? '管理员' : '用户' }}</span>
                </div>
                <el-icon class="chevron"><ArrowDown /></el-icon>
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
  background: var(--bg-primary);
}

.layout-glow {
  display: none;
}

.main-container {
  height: 100%;
}

.sidebar {
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
  transition: width var(--transition-normal);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--primary-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 800;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
}

.logo-mini {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ===== 菜单 ===== */
.sidebar-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  background: transparent !important;
  padding: 8px 12px;
}

.menu-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--text-muted);
  padding: 16px 12px 6px;
}

.el-menu--collapse .menu-label {
  display: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  border-radius: 8px !important;
  margin: 2px 0;
  color: var(--text-secondary) !important;
  transition: all var(--transition-fast);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: #f5f7fa !important;
  color: var(--text-primary) !important;
}

:deep(.el-menu-item.is-active) {
  background: rgba(6, 182, 212, 0.08) !important;
  color: var(--primary-color) !important;
}

:deep(.el-menu-item.is-active) .el-icon {
  color: var(--primary-color) !important;
}

:deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

:deep(.el-sub-menu .el-menu .el-menu-item) {
  padding-left: 48px !important;
}

/* ===== 顶栏 ===== */
.header {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px !important;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  cursor: pointer;
  color: var(--text-muted);
  padding: 6px;
  border-radius: 6px;
  transition: all var(--transition-fast);
}

.collapse-btn:hover {
  color: var(--primary-color);
  background: rgba(6, 182, 212, 0.1);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-badge :deep(.el-badge__content) {
  background: var(--danger-color);
  border: none;
  font-size: 11px;
}

.header-divider {
  width: 1px;
  height: 24px;
  background: var(--border-color);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 10px;
  transition: all var(--transition-fast);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.05);
}

.user-text {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}

.user-role {
  font-size: 11px;
  color: var(--text-muted);
}

.user-avatar {
  background: var(--primary-gradient) !important;
  border: 2px solid rgba(6, 182, 212, 0.3);
}

.chevron {
  color: var(--text-muted);
  font-size: 14px;
}

/* ===== 主内容 ===== */
.main-content {
  background: transparent;
  padding: 24px;
  overflow-y: auto;
  height: calc(100vh - 64px);
}

/* 主内容区域入口动画 */
.main-content :deep(.el-card:first-child) {
  animation: fadeSlideUp 0.5s ease-out;
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
