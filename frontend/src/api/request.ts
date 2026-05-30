import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

/**
 * axios 实例。
 * baseURL = /api → 由 vite.config.ts 的 proxy 转发到网关 8080（开发期免 CORS）。
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 60000 // LLM 响应慢，给足超时
})

// 请求拦截：自动带上 JWT
request.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

// 响应拦截：401（token 失效）→ 清登录态并回登录页
request.interceptors.response.use(
  (resp) => resp,
  (error) => {
    if (error.response && error.response.status === 401) {
      useAuthStore().logout()
      if (location.pathname !== '/login') {
        location.assign('/login')
      }
    }
    return Promise.reject(error)
  }
)

export default request
