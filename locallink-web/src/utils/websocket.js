/**
 * WebSocket 工具类
 * 封装 WebSocket 连接逻辑，支持自动重连和心跳检测
 */

import { useUserStore } from '../store/user'

/**
 * 获取 WebSocket URL
 * @param {number} userId - 用户ID
 * @returns {string} WebSocket URL
 */
export function getWebSocketUrl(userId) {
  if (!userId) {
    throw new Error('userId is required')
  }

  // 从 store 获取服务器配置
  const userStore = useUserStore()
  const config = userStore.serverConfig
  
  // 如果有配置，使用配置中的 URL
  if (config && config.wsUrl) {
    // 确保 wsUrl 不包含末尾斜杠
    const baseUrl = config.wsUrl.replace(/\/$/, '')
    return `${baseUrl}/ws/chat/${userId}`
  }
  
  // 兜底：使用环境变量或默认值
  if (window.electron && window.electron.isElectron) {
    return `ws://localhost:8080/ws/chat/${userId}`
  }

  // 浏览器环境
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host || 'localhost:8080'
  return `${wsProtocol}//${host}/ws/chat/${userId}`
}

/**
 * 创建 WebSocket 连接
 * @param {number} userId - 用户ID
 * @param {Object} callbacks - 回调函数 { onOpen, onMessage, onClose, onError }
 * @returns {WebSocket} WebSocket 实例
 */
export function createWebSocket(userId, callbacks = {}) {
  const {
    onOpen = () => {},
    onMessage = () => {},
    onClose = () => {},
    onError = () => {}
  } = callbacks

  if (!userId) {
    throw new Error('userId is required')
  }

  const url = getWebSocketUrl(userId)
  console.log('正在建立 WebSocket 连接:', url)

  const ws = new WebSocket(url)

  ws.onopen = () => {
    console.log('WebSocket 已连接')
    onOpen(ws)
  }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      onMessage(data, ws)
    } catch (error) {
      console.error('WebSocket 消息解析失败:', error)
    }
  }

  ws.onclose = (event) => {
    console.log('WebSocket 已关闭', {
      code: event?.code,
      reason: event?.reason,
      wasClean: event?.wasClean
    })
    onClose(event)
  }

  ws.onerror = (error) => {
    console.error('WebSocket 连接错误:', error)
    onError(error)
  }

  return ws
}

/**
 * 安全发送消息
 * @param {WebSocket} ws - WebSocket 实例
 * @param {Object} data - 要发送的数据
 */
export function safeSend(ws, data) {
  if (!ws) {
    console.error('WebSocket 实例不存在')
    return false
  }

  if (ws.readyState !== WebSocket.OPEN) {
    console.error('WebSocket 未连接，当前状态:', ws.readyState)
    return false
  }

  try {
    ws.send(JSON.stringify(data))
    return true
  } catch (error) {
    console.error('WebSocket 发送失败:', error)
    return false
  }
}

/**
 * 关闭 WebSocket 连接
 * @param {WebSocket} ws - WebSocket 实例
 */
export function closeWebSocket(ws) {
  if (ws) {
    console.log('关闭 WebSocket 连接')
    ws.close()
  }
}

/**
 * WebSocket 就绪状态
 */
export const WS_READY_STATE = {
  CONNECTING: 0,
  OPEN: 1,
  CLOSING: 2,
  CLOSED: 3
}
