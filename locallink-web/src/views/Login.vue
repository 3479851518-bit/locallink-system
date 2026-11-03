<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>LocalLink 登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="账号" prop="account">
          <el-input v-model="form.account" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <div class="links">
          <router-link to="/register">注册账号</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, getServerConfig } from '../api/user'
import { useUserStore } from '../store/user'
import { createWebSocket } from '../utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    // 1. 先获取服务器配置
    let configRes
    try {
      configRes = await getServerConfig()
      userStore.setServerConfig(configRes.data)
      console.log('服务器配置已加载:', configRes.data)
    } catch (configError) {
      console.warn('获取服务器配置失败，将使用默认配置:', configError)
      // 如果获取失败，使用默认配置
      userStore.setServerConfig({
        baseUrl: 'http://localhost:8080',
        wsUrl: 'ws://localhost:8080',
        serverHost: 'localhost',
        serverPort: '8080'
      })
    }
    
    // 2. 登录
    const res = await login(form)
    
    // 确保id存在
    if (!res.data || !res.data.id) {
      ElMessage.error('用户信息异常')
      return
    }
    
    // 使用 store 统一管理状态（已包含 localStorage 同步）
    userStore.setUserInfo(res.data)
    const token = 'token_' + Date.now()
    userStore.setToken(token)
    
    // 使用 WebSocket 工具建立连接
    const ws = createWebSocket(res.data.id, {
      onMessage: (data) => {
        console.log('收到消息:', data)
      }
    })
    userStore.setWebSocket(ws)
    
    ElMessage.success('登录成功')
    router.push('/chat')
  } catch (error) {
    console.error('登录失败:', error)
    // 显示后端返回的错误信息
    const errorMessage = error.response?.data?.message || error.message || '登录失败，请检查账号和密码'
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.links {
  text-align: center;
  margin-top: 10px;
}
</style>
