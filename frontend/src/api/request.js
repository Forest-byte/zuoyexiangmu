import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 20000
})

// 请求拦截：附加 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('erp_token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

// 响应拦截：统一处理 code
request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code === 200) {
      return data.data
    }
    if (data.code === 401) {
      localStorage.removeItem('erp_token')
      router.push('/login')
      ElMessage.error(data.message || '登录已过期')
      return Promise.reject(new Error(data.message))
    }
    if (data.code === 403) {
      ElMessage.warning(data.message || '无权限执行该操作')
      return Promise.reject(new Error(data.message))
    }
    ElMessage.error(data.message || '操作失败')
    return Promise.reject(new Error(data.message))
  },
  err => {
    ElMessage.error(err.response?.data?.message || err.message || '网络异常')
    return Promise.reject(err)
  }
)

export default request
