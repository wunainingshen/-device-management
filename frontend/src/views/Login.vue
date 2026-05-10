<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">
          <div class="logo-icon"><el-icon :size="22"><Monitor /></el-icon></div>
          <span>DeviceFlow</span>
        </div>
        <h2>欢迎回来</h2>
        <p>登录到设备管理系统</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs" :stretch="true">
        <el-tab-pane label="用户登录" name="user">
          <el-form ref="userFormRef" :model="userForm" :rules="rules" class="login-form">
            <el-form-item prop="username">
              <el-input v-model="userForm.username" placeholder="用户名" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="userForm.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="login-btn" @click="handleLogin" :loading="loading">
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="管理员登录" name="admin">
          <el-form ref="adminFormRef" :model="adminForm" :rules="rules" class="login-form">
            <el-form-item prop="username">
              <el-input v-model="adminForm.username" placeholder="管理员用户名" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="adminForm.password" type="password" placeholder="管理员密码" :prefix-icon="Lock" size="large" show-password />
            </el-form-item>
            <el-form-item>
              <el-button size="large" class="login-btn admin-btn" @click="handleAdminLogin" :loading="loading">
                管理员登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="login-footer">
        <span>还没有账户？</span>
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  authStore.token = ''
  authStore.user = null
})

const activeTab = ref('user')
const loading = ref(false)
const userFormRef = ref(null)
const adminFormRef = ref(null)

const userForm = reactive({ username: '', password: '' })
const adminForm = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await userFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login(userForm)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleAdminLogin = async () => {
  const valid = await adminFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login(adminForm)
    if (authStore.userRole !== 'ADMIN') {
      authStore.logout()
      ElMessage.error('该账户不是管理员')
      return
    }
    ElMessage.success('管理员登录成功')
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8ecf1 100%);
  position: relative;
  overflow: hidden;
}

.login-bg { display: none; }
.bg-orb { display: none; }
.bg-orb-1 { display: none; }
.bg-orb-2 { display: none; }
.bg-orb-3 { display: none; }
.bg-grid { display: none; }

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(30px, -30px) rotate(2deg); }
  66% { transform: translate(-20px, 20px) rotate(-1deg); }
}

/* ===== 登录卡片 ===== */
.login-card {
  width: 420px;
  padding: 40px;
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 1;
  animation: cardEnter 0.6s ease-out;
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(24px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
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
}

.login-logo span {
  font-size: 20px;
  font-weight: 800;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.login-header h2 {
  font-size: 24px;
  color: var(--text-primary);
  margin-bottom: 6px;
  font-weight: 700;
}

.login-header p {
  color: var(--text-muted);
  font-size: 14px;
}

/* ===== 选项卡 ===== */
.login-tabs {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  color: var(--text-muted) !important;
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-tabs__item.is-active) {
  color: var(--primary-light) !important;
}

:deep(.el-tabs__active-bar) {
  background: var(--primary-color) !important;
}

:deep(.el-tabs__nav-wrap::after) {
  background: var(--border-color) !important;
}

/* ===== 登录表单 ===== */
.login-form {
  margin-top: 8px;
}

:deep(.el-input__wrapper) {
  padding: 4px 16px;
  height: 48px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  border-radius: 10px !important;
  margin-top: 8px;
}

.admin-btn {
  background: linear-gradient(135deg, #f43f5e, #e11d48) !important;
  border: none !important;
  color: #fff !important;
}

.admin-btn:hover {
  box-shadow: 0 4px 20px rgba(244, 63, 94, 0.3) !important;
  transform: translateY(-1px);
}

.login-footer {
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
  margin-top: 4px;
}

.register-link {
  color: var(--primary-light);
  text-decoration: none;
  font-weight: 600;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
