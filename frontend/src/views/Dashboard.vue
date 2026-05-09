<template>
  <div class="dashboard">
    <div class="welcome-card">
      <div class="welcome-text">
        <h2>欢迎回来，{{ authStore.currentUser?.nickname || authStore.currentUser?.username }}</h2>
        <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
      </div>
      <div class="welcome-icon">
        <el-icon :size="80" color="rgba(64,158,255,0.2)"><Monitor /></el-icon>
      </div>
    </div>

    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-label">设备总数</div>
            <div class="stat-value">{{ stats.totalDevices }}</div>
          </div>
          <div class="stat-icon blue">
            <el-icon :size="32"><Monitor /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-label">正常运行</div>
            <div class="stat-value success">{{ stats.normalDevices }}</div>
          </div>
          <div class="stat-icon green">
            <el-icon :size="32"><SuccessFilled /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-label">故障维修</div>
            <div class="stat-value warning">{{ stats.faultDevices }}</div>
          </div>
          <div class="stat-icon orange">
            <el-icon :size="32"><WarningFilled /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-label">在线用户</div>
            <div class="stat-value info">{{ onlineCount }}</div>
          </div>
          <div class="stat-icon purple">
            <el-icon :size="32"><UserFilled /></el-icon>
          </div>
        </div>
      </el-card>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="section-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>设备状态分布</span>
            </div>
          </template>
          <div class="chart-wrapper">
            <div ref="pieChartRef" style="height: 320px;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="section-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" class="action-btn" @click="$router.push('/devices')" v-if="authStore.isAdminUser">
              <el-icon><Plus /></el-icon> 管理设备
            </el-button>
            <el-button type="success" class="action-btn" @click="$router.push('/chat')">
              <el-icon><ChatDotRound /></el-icon> 在线聊天
            </el-button>
            <el-button type="warning" class="action-btn" @click="$router.push('/ai')">
              <el-icon><MagicStick /></el-icon> AI 助手
            </el-button>
            <el-button type="info" class="action-btn" @click="$router.push('/profile')">
              <el-icon><Edit /></el-icon> 修改信息
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useAuthStore } from '../store/auth'
import { useChatStore } from '../store/chat'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import * as echarts from 'echarts'

const authStore = useAuthStore()
const chatStore = useChatStore()
const router = useRouter()

const pieChartRef = ref(null)

const currentDate = computed(() => {
  const now = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${weekdays[now.getDay()]}`
})

const stats = ref({
  totalDevices: 0,
  normalDevices: 0,
  faultDevices: 0
})

const onlineCount = ref(0)

const fetchStats = async () => {
  try {
    const res = await axios.get('/api/admin/devices')
    if (res.data.code === 200) {
      const devices = res.data.data
      stats.value.totalDevices = devices.length
      stats.value.normalDevices = devices.filter(d => d.status === 'NORMAL').length
      stats.value.faultDevices = devices.filter(d => d.status === 'FAULT' || d.status === 'MAINTENANCE').length
    }
  } catch (e) {
    // Non-admin users may not have access
  }
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { fontSize: 13 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        fontSize: 12
      },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold' }
      },
      data: [
        { value: stats.value.normalDevices, name: '正常运行', itemStyle: { color: '#67c23a' } },
        { value: stats.value.faultDevices, name: '故障维修', itemStyle: { color: '#e6a23c' } },
        { value: stats.value.totalDevices - stats.value.normalDevices - stats.value.faultDevices, name: '其他', itemStyle: { color: '#909399' } }
      ]
    }]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

onMounted(async () => {
  await fetchStats()
  nextTick(() => initPieChart())
  onlineCount.value = chatStore.friends.filter(f => f.isOnline).length
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 30px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.welcome-card::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255,255,255,0.05);
  top: -100px;
  right: -50px;
}

.welcome-text h2 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.welcome-text p {
  font-size: 14px;
  opacity: 0.85;
}

.welcome-icon {
  position: relative;
  z-index: 1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
}

.stat-value.success { color: #67c23a; }
.stat-value.warning { color: #e6a23c; }
.stat-value.info { color: #409eff; }

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.blue { background: rgba(64,158,255,0.1); color: #409eff; }
.stat-icon.green { background: rgba(103,194,58,0.1); color: #67c23a; }
.stat-icon.orange { background: rgba(230,162,60,0.1); color: #e6a23c; }
.stat-icon.purple { background: rgba(102,126,234,0.1); color: #667eea; }

.section-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.chart-wrapper {
  padding: 10px 0;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  height: 44px;
  font-size: 14px;
  justify-content: flex-start;
  border-radius: 10px;
}
</style>
