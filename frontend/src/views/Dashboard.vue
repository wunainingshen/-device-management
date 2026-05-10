<template>
  <div class="dashboard">
    <div class="welcome-card">
      <div class="welcome-bg"></div>
      <div class="welcome-content">
        <div class="welcome-text">
          <div class="welcome-greeting">
            <span class="greeting-dot"></span> 欢迎回来
          </div>
          <h2>{{ authStore.currentUser?.nickname || authStore.currentUser?.username }}</h2>
          <p>{{ currentDate }}，今天适合高效工作</p>
        </div>
        <div class="welcome-illus">
          <div class="illus-ring"></div>
          <el-icon :size="48" class="illus-icon"><Monitor /></el-icon>
        </div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-bg" style="background:linear-gradient(135deg,rgba(6,182,212,0.08),rgba(6,182,212,0.02))"></div>
        <div class="stat-content">
          <div><div class="stat-label">设备总数</div><div class="stat-value">{{ stats.totalDevices }}</div></div>
          <div class="stat-icon-wrap" style="background:rgba(6,182,212,0.15);color:var(--primary-color)">
            <el-icon :size="22"><Monitor /></el-icon>
          </div>
        </div>
        <div class="stat-trend"><span class="trend-up">12.5%</span><span class="trend-label"> 较上月</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-bg" style="background:linear-gradient(135deg,rgba(16,185,129,0.08),rgba(16,185,129,0.02))"></div>
        <div class="stat-content">
          <div><div class="stat-label">正常运行</div><div class="stat-value success">{{ stats.normalDevices }}</div></div>
          <div class="stat-icon-wrap" style="background:rgba(16,185,129,0.15);color:var(--success-color)">
            <el-icon :size="22"><SuccessFilled /></el-icon>
          </div>
        </div>
        <div class="stat-trend"><span class="trend-up">8.3%</span><span class="trend-label"> 较上月</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-bg" style="background:linear-gradient(135deg,rgba(245,158,11,0.08),rgba(245,158,11,0.02))"></div>
        <div class="stat-content">
          <div><div class="stat-label">故障维修</div><div class="stat-value warning">{{ stats.faultDevices }}</div></div>
          <div class="stat-icon-wrap" style="background:rgba(245,158,11,0.15);color:var(--warning-color)">
            <el-icon :size="22"><WarningFilled /></el-icon>
          </div>
        </div>
        <div class="stat-trend"><span class="trend-down">3.1%</span><span class="trend-label"> 较上月</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-bg" style="background:linear-gradient(135deg,rgba(99,102,241,0.08),rgba(99,102,241,0.02))"></div>
        <div class="stat-content">
          <div><div class="stat-label">在线用户</div><div class="stat-value info">{{ onlineCount }}</div></div>
          <div class="stat-icon-wrap" style="background:rgba(99,102,241,0.15);color:var(--info-color)">
            <el-icon :size="22"><UserFilled /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <el-row :gutter="24">
      <el-col :span="16">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-header-left"><div class="card-header-dot"></div><span>设备状态分布</span></div>
              <span class="card-subtitle">实时</span>
            </div>
          </template>
          <div class="chart-wrap"><div ref="pieChartRef" style="height:320px"></div></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-header-left"><div class="card-header-dot" style="background:var(--warning-color)"></div><span>快速操作</span></div>
            </div>
          </template>
          <div class="quick-actions">
            <div class="qa-item" @click="$router.push('/devices')" v-if="authStore.isAdminUser">
              <div class="qa-icon" style="background:rgba(6,182,212,0.12);color:var(--primary-color)"><el-icon :size="20"><Plus /></el-icon></div>
              <div class="qa-text"><span class="qa-title">管理设备</span><span class="qa-desc">添加或编辑设备信息</span></div>
              <el-icon class="qa-arrow" color="var(--text-muted)"><ArrowRight /></el-icon>
            </div>
            <div class="qa-item" @click="$router.push('/chat')">
              <div class="qa-icon" style="background:rgba(16,185,129,0.12);color:var(--success-color)"><el-icon :size="20"><ChatDotRound /></el-icon></div>
              <div class="qa-text"><span class="qa-title">在线聊天</span><span class="qa-desc">与同事实时沟通</span></div>
              <el-icon class="qa-arrow" color="var(--text-muted)"><ArrowRight /></el-icon>
            </div>
            <div class="qa-item" @click="$router.push('/ai')">
              <div class="qa-icon" style="background:rgba(99,102,241,0.12);color:var(--info-color)"><el-icon :size="20"><MagicStick /></el-icon></div>
              <div class="qa-text"><span class="qa-title">AI 助手</span><span class="qa-desc">智能问答与建议</span></div>
              <el-icon class="qa-arrow" color="var(--text-muted)"><ArrowRight /></el-icon>
            </div>
            <div class="qa-item" @click="$router.push('/profile')">
              <div class="qa-icon" style="background:rgba(245,158,11,0.12);color:var(--warning-color)"><el-icon :size="20"><Edit /></el-icon></div>
              <div class="qa-text"><span class="qa-title">修改信息</span><span class="qa-desc">更新个人资料</span></div>
              <el-icon class="qa-arrow" color="var(--text-muted)"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useAuthStore } from '../store/auth'
import { useChatStore } from '../store/chat'
import { useRouter } from 'vue-router'
import { Monitor, Plus, ChatDotRound, MagicStick, Edit, ArrowRight, SuccessFilled, WarningFilled, UserFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import axios from '../utils/axios'

const authStore = useAuthStore()
const chatStore = useChatStore()
const router = useRouter()
const pieChartRef = ref(null)

const now = new Date()
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const currentDate = `${now.getFullYear()}年${now.getMonth()+1}月${now.getDate()}日 星期${weekdays[now.getDay()]}`

const stats = ref({ totalDevices: 0, normalDevices: 0, faultDevices: 0 })
const onlineCount = ref(0)

const fetchStats = async () => {
  try {
    const res = await axios.get('/api/admin/devices')
    if (res.data.code === 200) {
      const d = res.data.data
      stats.value = {
        totalDevices: d.length,
        normalDevices: d.filter(x => x.status === 'NORMAL').length,
        faultDevices: d.filter(x => x.status === 'FAULT' || x.status === 'MAINTENANCE').length
      }
    }
  } catch (e) { /* non-admin */ }
}

onMounted(async () => {
  await fetchStats()
  onlineCount.value = chatStore.friends.filter(f => f.isOnline).length
  nextTick(() => {
    if (!pieChartRef.value) return
    const chart = echarts.init(pieChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item', backgroundColor: 'rgba(15,23,42,0.9)', borderColor: 'rgba(148,163,184,0.1)', textStyle: { color: '#f1f5f9' } },
      legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { color: '#94a3b8', fontSize: 13 } },
      series: [{
        type: 'pie', radius: ['42%', '72%'], center: ['35%', '50%'],
        itemStyle: { borderRadius: 6, borderColor: 'rgba(10,14,26,0.8)', borderWidth: 3 },
        label: { show: true, formatter: '{d}%', color: '#94a3b8', fontSize: 12 },
        data: [
          { value: stats.value.normalDevices, name: '正常运行', itemStyle: { color: '#10b981' } },
          { value: stats.value.faultDevices, name: '故障维修', itemStyle: { color: '#f59e0b' } },
          { value: Math.max(0, stats.value.totalDevices - stats.value.normalDevices - stats.value.faultDevices), name: '其他', itemStyle: { color: '#64748b' } }
        ]
      }]
    })
    window.addEventListener('resize', () => chart.resize())
  })
})
</script>

<style scoped>
.dashboard { max-width: 1400px; margin: 0 auto; animation: fadeSlideUp 0.5s ease-out; }
@keyframes fadeSlideUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }

.welcome-card {
  position: relative; overflow: hidden;
  background: linear-gradient(135deg, #e8f4f8 0%, #f0f2f5 100%);
  border: 1px solid var(--border-color); border-radius: var(--radius-xl);
  padding: 32px 40px; margin-bottom: 24px;
}
.welcome-bg { display: none; }
.welcome-content { display: flex; justify-content: space-between; align-items: center; position: relative; z-index: 1; }
.welcome-greeting { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--primary-light); font-weight: 500; margin-bottom: 8px; }
.greeting-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--primary-color); animation: pulse 2s ease-in-out infinite; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } }
.welcome-text h2 { font-size: 26px; font-weight: 700; color: var(--text-primary); margin-bottom: 6px; }
.welcome-text p { font-size: 14px; color: var(--text-muted); }
.welcome-illus { position: relative; display: flex; align-items: center; justify-content: center; }
.illus-ring { position: absolute; width: 80px; height: 80px; border-radius: 50%; border: 1px solid rgba(6,182,212,0.2); animation: spin 20s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.illus-icon { color: rgba(6,182,212,0.3); }

.stats-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 20px; margin-bottom: 24px; }
.stat-card {
  position: relative; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--radius-lg);
  padding: 20px 24px; overflow: hidden; cursor: default; transition: all var(--transition-normal);
}
.stat-card:hover { border-color: rgba(148,163,184,0.4); transform: translateY(-2px); box-shadow: 0 8px 30px rgba(0,0,0,0.08); }
.stat-bg { position: absolute; inset: 0; pointer-events: none; }
.stat-content { display: flex; justify-content: space-between; align-items: flex-start; position: relative; z-index: 1; }
.stat-label { font-size: 13px; color: var(--text-muted); font-weight: 500; margin-bottom: 8px; }
.stat-value { font-size: 32px; font-weight: 700; color: var(--text-primary); line-height: 1; }
.stat-value.success { color: var(--success-color); }
.stat-value.warning { color: var(--warning-color); }
.stat-value.info { color: var(--info-color); }
.stat-icon-wrap { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-trend { margin-top: 12px; font-size: 12px; position: relative; z-index: 1; }
.trend-up { color: var(--success-color); font-weight: 600; }
.trend-down { color: var(--danger-color); font-weight: 600; }
.trend-label { color: var(--text-muted); }

.section-card { margin-bottom: 24px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header-left { display: flex; align-items: center; gap: 10px; font-weight: 600; font-size: 15px; color: var(--text-primary); }
.card-header-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--primary-color); }
.card-subtitle { font-size: 12px; color: var(--text-muted); }
.chart-wrap { padding: 10px 0; }

.quick-actions { display: flex; flex-direction: column; gap: 4px; }
.qa-item { display: flex; align-items: center; gap: 14px; padding: 14px; border-radius: 10px; cursor: pointer; transition: all var(--transition-fast); }
.qa-item:hover { background: rgba(255,255,255,0.03); }
.qa-item:hover .qa-arrow { color: var(--text-secondary) !important; transform: translateX(2px); }
.qa-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.qa-text { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.qa-title { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.qa-desc { font-size: 12px; color: var(--text-muted); }
.qa-arrow { transition: all var(--transition-fast); }
</style>
