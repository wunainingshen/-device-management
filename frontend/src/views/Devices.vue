<template>
  <div class="devices-page">
    <el-card class="page-card" shadow="hover">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <el-icon :size="22" color="#409eff"><Monitor /></el-icon>
            <span>设备管理</span>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="showImportDialog" :icon="Upload">导入</el-button>
            <el-button type="success" @click="handleExport" :icon="Download">导出</el-button>
            <el-button type="primary" @click="openDialog(null)" :icon="Plus">添加设备</el-button>
          </div>
        </div>
      </template>

      <!-- Search -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="设备名称">
            <el-input v-model="searchForm.name" placeholder="搜索设备名称" clearable />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.category" placeholder="选择分类" clearable style="width:150px">
              <el-option label="办公设备" value="办公设备" />
              <el-option label="生产设备" value="生产设备" />
              <el-option label="实验设备" value="实验设备" />
              <el-option label="网络设备" value="网络设备" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width:150px">
              <el-option label="正常运行" value="NORMAL" />
              <el-option label="故障" value="FAULT" />
              <el-option label="维护中" value="MAINTENANCE" />
              <el-option label="已报废" value="SCRAPPED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchDevices" :icon="Search">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Table -->
      <el-table :data="devices" stripe style="width: 100%" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="设备名称" min-width="150" />
        <el-table-column prop="model" label="型号" width="130" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="brand" label="品牌" width="120" />
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain" round>
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="130" />
        <el-table-column prop="purchaseDate" label="购买日期" width="120">
          <template #default="{ row }">{{ formatDate(row.purchaseDate) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDialog(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row.id)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Device Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false">
      <el-form ref="deviceFormRef" :model="deviceForm" :rules="deviceRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="deviceForm.name" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="deviceForm.model" placeholder="请输入型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="deviceForm.category" placeholder="选择分类" style="width:100%">
                <el-option label="办公设备" value="办公设备" />
                <el-option label="生产设备" value="生产设备" />
                <el-option label="实验设备" value="实验设备" />
                <el-option label="网络设备" value="网络设备" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="deviceForm.brand" placeholder="请输入品牌" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="deviceForm.status" placeholder="选择状态" style="width:100%">
                <el-option label="正常运行" value="NORMAL" />
                <el-option label="故障" value="FAULT" />
                <el-option label="维护中" value="MAINTENANCE" />
                <el-option label="已报废" value="SCRAPPED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="位置" prop="location">
              <el-input v-model="deviceForm.location" placeholder="请输入位置" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="deviceForm.description" type="textarea" :rows="2" placeholder="请输入设备描述" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="购买日期">
              <el-date-picker v-model="deviceForm.purchaseDate" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保修到期">
              <el-date-picker v-model="deviceForm.warrantyExpiry" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDevice" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- Import Dialog -->
    <el-dialog v-model="importVisible" title="导入设备数据" width="420px">
      <div class="import-content">
        <el-upload
          ref="uploadRef"
          accept=".xlsx,.xls"
          :auto-upload="false"
          :show-file-list="true"
          :limit="1"
          :on-change="handleFileChange"
        >
          <el-button type="primary" :icon="Upload">选择Excel文件</el-button>
          <template #tip>
            <p class="upload-tip">支持 .xlsx / .xls 格式的文件</p>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Download, Search, Edit, Delete } from '@element-plus/icons-vue'
import axios from '../utils/axios'

const loading = ref(false)
const saving = ref(false)
const importing = ref(false)
const devices = ref([])
const dialogVisible = ref(false)
const importVisible = ref(false)
const dialogTitle = ref('添加设备')
const deviceFormRef = ref(null)
const selectedFile = ref(null)

const searchForm = reactive({
  name: '',
  category: '',
  status: ''
})

const defaultForm = () => ({
  name: '',
  model: '',
  category: '',
  brand: '',
  status: 'NORMAL',
  description: '',
  location: '',
  purchaseDate: null,
  warrantyExpiry: null
})

const deviceForm = reactive(defaultForm())
const editingId = ref(null)

const deviceRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

const statusType = (status) => {
  const map = { NORMAL: 'success', FAULT: 'danger', MAINTENANCE: 'warning', SCRAPPED: 'info' }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = { NORMAL: '正常运行', FAULT: '故障', MAINTENANCE: '维护中', SCRAPPED: '已报废' }
  return map[status] || status
}

const formatDate = (date) => {
  if (!date) return '-'
  return date.substring(0, 10)
}

const fetchDevices = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/devices')
    if (res.data.code === 200) {
      devices.value = res.data.data
    }
  } catch (e) {
    ElMessage.error('获取设备列表失败')
  } finally {
    loading.value = false
  }
}

const searchDevices = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (searchForm.name) params.append('name', searchForm.name)
    if (searchForm.category) params.append('category', searchForm.category)
    if (searchForm.status) params.append('status', searchForm.status)
    const res = await axios.get(`/api/admin/devices/search?${params}`)
    if (res.data.code === 200) {
      devices.value = res.data.data
    }
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.category = ''
  searchForm.status = ''
  fetchDevices()
}

const openDialog = (device) => {
  if (device) {
    dialogTitle.value = '编辑设备'
    editingId.value = device.id
    Object.assign(deviceForm, device)
  } else {
    dialogTitle.value = '添加设备'
    editingId.value = null
    Object.assign(deviceForm, defaultForm())
  }
  dialogVisible.value = true
}

const saveDevice = async () => {
  const valid = await deviceFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingId.value) {
      await axios.put(`/api/admin/devices/${editingId.value}`, deviceForm)
      ElMessage.success('设备更新成功')
    } else {
      await axios.post('/api/admin/devices', deviceForm)
      ElMessage.success('设备添加成功')
    }
    dialogVisible.value = false
    fetchDevices()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该设备吗？此操作不可撤销。', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.delete(`/api/admin/devices/${id}`)
    ElMessage.success('设备已删除')
    fetchDevices()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleExport = async () => {
  try {
    const res = await axios.get('/api/device/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `设备数据_${new Date().toISOString().slice(0,10)}.xlsx`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

const showImportDialog = () => {
  selectedFile.value = null
  importVisible.value = true
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    const res = await axios.post('/api/device/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.code === 200) {
      ElMessage.success(res.data.message)
      importVisible.value = false
      fetchDevices()
    }
  } catch (e) {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(fetchDevices)
</script>

<style scoped>
.devices-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-card {
  border-radius: 12px;
  border: none;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
}

.header-right {
  display: flex;
  gap: 10px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.import-content {
  text-align: center;
  padding: 30px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>
