import axios from 'axios'
import { ElMessage } from 'element-plus'

// 使用环境变量配置 API 基础 URL
const getBackendUrl = () => {
  // 优先使用环境变量
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  
  // Electron 环境
  if (window.electron && window.electron.isElectron) {
    return 'http://localhost:8080/api'
  }
  
  // 浏览器开发环境使用代理
  if (import.meta.env.DEV) {
    return '/api'
  }
  
  // 生产环境
  return '/api'
}

const request = axios.create({
  baseURL: getBackendUrl(),
  timeout: 30000
})

// 请求拦截器 - 自动携带 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 文件下载等二进制响应直接返回
    if (response.config.responseType === 'blob' || response.data instanceof Blob) {
      return response.data
    }

    const res = response.data
    if (!res || typeof res !== 'object' || res.code == null) {
      return res
    }

    if (Number(res.code) !== 200) {
      const msg = res.message || '请求失败'
      ElMessage.error(msg)
      return Promise.reject(new Error(msg))
    }
    return res
  },
  error => {
    // 长轮询等标记为跳过的请求，由调用方自行处理
    if (error.config?.skipErrorHandler) {
      return Promise.reject(error)
    }

    // 处理 401 未授权
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    
    // 处理连接错误
    if (error.code === 'ECONNREFUSED' || error.code === 'ERR_NETWORK') {
      ElMessage.error('无法连接到服务器，请确保后端服务已启动')
    } else if (error.response) {
      // 服务器返回错误状态码
      const errorMsg = error.response.data?.message || error.response.data?.msg || `请求失败 (${error.response.status})`
      ElMessage.error(errorMsg)
    } else if (error.code === 'ECONNABORTED' && error.message?.includes('timeout')) {
      // 请求超时（非长轮询场景才提示）
      ElMessage.error('请求超时，请检查网络或稍后重试')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
