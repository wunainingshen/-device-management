<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>设备管理系统</h2>
        <p>欢迎回来，请登录您的账户</p>
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
              <el-button type="danger" size="large" class="login-btn" @click="handleAdminLogin" :loading="loading">
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

// 每次进入登录页先清除旧的缓存token
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

const userForm = reactive({
  username: '',
  password: ''
})

const adminForm = reactive({
  username: '',
  password: ''
})

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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 2000px;
  height: 2000px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%);
  top: -1000px;
  right: -500px;
  animation: float 15s ease-in-out infinite;
}

.login-container::after {
  content: '';
  position: absolute;
  width: 1500px;
  height: 1500px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255,255,255,0.08) 0%, rgba(255,255,255,0.03) 100%);
  bottom: -800px;
  left: -300px;
  animation: float 20s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(30px, -30px) rotate(5deg); }
  66% { transform: translate(-20px, 20px) rotate(-3deg); }
}

.login-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-tabs {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
}

.login-form {
  margin-top: 10px;
}

:deep(.el-input__wrapper) {
  padding: 4px 15px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 10px;
  margin-top: 10px;
}

.login-footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.register-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  margin-left: 5px;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
