import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const token = ref(localStorage.getItem('token') || '')
  const ws = ref(null) // 保留用于兼容
  const httpPolling = ref(null) // HTTP长轮询控制器
  const serverConfig = ref(JSON.parse(localStorage.getItem('serverConfig') || 'null'))

  function setUserInfo(user) {
    userInfo.value = user
    // 同步到localStorage
    if (user) {
      localStorage.setItem('userInfo', JSON.stringify(user))
    }
  }

  function setToken(tok) {
    token.value = tok
    // 同步到localStorage
    if (tok) {
      localStorage.setItem('token', tok)
    }
  }

  function setWebSocket(websocket) {
    ws.value = websocket
  }

  function setHttpPolling(polling) {
    httpPolling.value = polling
  }

  function setServerConfig(config) {
    serverConfig.value = config
    if (config) {
      localStorage.setItem('serverConfig', JSON.stringify(config))
    }
  }

  function logout() {
    userInfo.value = null
    token.value = ''
    serverConfig.value = null
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    localStorage.removeItem('serverConfig')
    
    // 关闭WebSocket（如果存在）
    if (ws.value) {
      ws.value.close()
      ws.value = null
    }
    
    // 停止HTTP轮询（如果存在）
    if (httpPolling.value) {
      httpPolling.value.stop()
      httpPolling.value = null
    }
  }

  return {
    userInfo,
    token,
    ws,
    httpPolling,
    serverConfig,
    setUserInfo,
    setToken,
    setWebSocket,
    setHttpPolling,
    setServerConfig,
    logout
  }
})
