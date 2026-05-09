import { defineStore } from 'pinia'
import axios from '../utils/axios'
import router from '../router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    isAdmin: () => {
      const user = JSON.parse(localStorage.getItem('user') || 'null')
      return user?.role === 'ADMIN'
    }
  }),

  getters: {
    isLoggedIn: state => !!state.token,
    currentUser: state => state.user,
    userRole: state => state.user?.role || '',
    isAdminUser: state => state.user?.role === 'ADMIN'
  },

  actions: {
    async login(credentials) {
      console.log('[登录] 发送登录请求...')
      const res = await axios.post('/api/auth/login', credentials)
      console.log('[登录] 响应:', res.data.code, res.data.message)
      if (res.data.code === 200) {
        this.token = res.data.data.token
        this.user = res.data.data.user
        localStorage.setItem('token', this.token)
        localStorage.setItem('user', JSON.stringify(this.user))
        console.log('[登录] Token已存储到localStorage:', this.token.substring(0,30) + '...')
        console.log('[登录] localStorage中的token:', localStorage.getItem('token')?.substring(0,30) + '...')
        return true
      }
      throw new Error(res.data.message)
    },

    async register(data) {
      const res = await axios.post('/api/auth/register', data)
      if (res.data.code === 200) {
        return true
      }
      throw new Error(res.data.message)
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    },

    async fetchUserInfo() {
      try {
        const res = await axios.get('/api/user/info')
        if (res.data.code === 200) {
          this.user = res.data.data
          localStorage.setItem('user', JSON.stringify(this.user))
        }
      } catch (e) {
        console.error('Failed to fetch user info', e)
      }
    },

    async updateUserInfo(data) {
      const res = await axios.put('/api/user/update', data)
      if (res.data.code === 200) {
        this.user = res.data.data
        localStorage.setItem('user', JSON.stringify(this.user))
        return true
      }
      throw new Error(res.data.message)
    }
  }
})
