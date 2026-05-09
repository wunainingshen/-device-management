import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const instance = axios.create({
  baseURL: '',
  timeout: 30000
})

// Request interceptor
instance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log(`[请求] ${config.url} -> 已携带Token(${token.substring(0,20)}...)`)
    } else {
      console.warn(`[请求] ${config.url} -> localStorage中没有Token！`)
    }
    return config
  },
  error => Promise.reject(error)
)

// Response interceptor
instance.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          ElMessage.error('登录已过期，请重新登录')
          router.push('/login')
          return Promise.resolve({ data: { code: 401, message: '未登录' } })
        case 403:
          ElMessage.error('没有权限执行此操作')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查连接')
    }
    return Promise.reject(error)
  }
)

export default instance
