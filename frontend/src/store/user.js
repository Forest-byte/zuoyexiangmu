import { defineStore } from 'pinia'
import request from '@/api/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('erp_token') || '',
    user: null,
    menus: [],
    permissions: []
  }),
  actions: {
    async login(username, password) {
      const data = await request.post('/auth/login', { username, password })
      this.token = data.token
      this.user = data.user
      localStorage.setItem('erp_token', data.token)
      return data
    },
    async fetchInfo() {
      const data = await request.get('/auth/info')
      this.user = data.user
      this.menus = data.menus || []
      this.permissions = data.permissions || []
      return data
    },
    hasPerm(code) {
      if (!code) return true
      if (this.user && this.user.roleCode === 'ROLE_ADMIN') return true
      return this.permissions.includes(code)
    },
    logout() {
      this.token = ''
      this.user = null
      this.menus = []
      localStorage.removeItem('erp_token')
    }
  }
})
