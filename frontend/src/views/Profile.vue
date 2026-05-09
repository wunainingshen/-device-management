<template>
  <div class="profile-page">
    <el-row :gutter="24">
      <el-col :span="8">
        <el-card class="profile-card" shadow="hover">
          <div class="avatar-section">
            <el-avatar :size="100" :icon="UserFilled" class="avatar" />
            <h3>{{ authStore.currentUser?.nickname || authStore.currentUser?.username }}</h3>
            <el-tag :type="authStore.isAdminUser ? 'danger' : 'primary'" effect="plain" round>
              {{ authStore.isAdminUser ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item">
              <span class="label">用户名</span>
              <span class="value">{{ authStore.currentUser?.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮&emsp;箱</span>
              <span class="value">{{ authStore.currentUser?.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机号</span>
              <span class="value">{{ authStore.currentUser?.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间</span>
              <span class="value">{{ formatDate(authStore.currentUser?.createdAt) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <span class="card-title">修改个人信息</span>
          </template>
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" size="large" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" size="large" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" size="large" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="updating" size="large">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider />
          <span class="card-title">修改密码</span>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="password-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" size="large" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码 (6位以上)" size="large" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" size="large" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="handleChangePassword" :loading="changingPwd" size="large">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'
import axios from '../utils/axios'

const authStore = useAuthStore()
const updating = ref(false)
const changingPwd = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const profileForm = reactive({
  nickname: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== passwordForm.newPassword) callback(new Error('两次密码不一致'))
      else callback()
    }, trigger: 'blur' }
  ]
}

const formatDate = (date) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  const user = authStore.currentUser
  if (user) {
    profileForm.nickname = user.nickname || ''
    profileForm.email = user.email || ''
    profileForm.phone = user.phone || ''
  }
})

const handleUpdate = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  updating.value = true
  try {
    await authStore.updateUserInfo({
      nickname: profileForm.nickname,
      email: profileForm.email,
      phone: profileForm.phone
    })
    ElMessage.success('个人信息已更新')
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
  } finally {
    updating.value = false
  }
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  changingPwd.value = true
  try {
    await axios.put('/api/user/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '密码修改失败')
  } finally {
    changingPwd.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 20px;
}

.avatar-section {
  text-align: center;
  padding: 20px 0;
}

.avatar-section .avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  margin-bottom: 15px;
}

.avatar-section h3 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 10px;
}

.info-list {
  padding: 0 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #909399;
  font-size: 14px;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.password-form {
  margin-top: 20px;
}
</style>
