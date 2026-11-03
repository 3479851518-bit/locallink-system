/**
 * HTTP长轮询工具类
 * 替代WebSocket，使用HTTP长轮询实现实时消息推送
 */

import { useUserStore } from '../store/user'
import request from './request'

/** 服务端长轮询最长等待（秒） */
const POLL_SERVER_TIMEOUT_SEC = 30
/** 客户端 axios 超时须大于服务端等待时间，避免误报 timeout */
const POLL_CLIENT_TIMEOUT_MS = (POLL_SERVER_TIMEOUT_SEC + 15) * 1000

/**
 * 创建HTTP长轮询连接
 * @param {number} userId - 用户ID
 * @param {Object} callbacks - 回调函数 { onMessage, onError }
 * @returns {Object} 轮询控制器 { start, stop, sendHeartbeat, sendMessage }
 */
export function createHttpPolling(userId, callbacks = {}) {
  const {
    onMessage = () => {},
    onError = () => {}
  } = callbacks

  if (!userId) {
    throw new Error('userId is required')
  }

  let isRunning = false
  let pollTimer = null
  let heartbeatTimer = null
  let lastMessageId = null

  // 获取服务器配置
  const userStore = useUserStore()
  const baseUrl = userStore.serverConfig?.baseUrl || ''

  /**
   * 开始轮询
   */
  const start = () => {
    if (isRunning) {
      console.warn('HTTP轮询已在运行')
      return
    }

    isRunning = true
    console.log('✅ HTTP长轮询已启动')

    // 立即发送第一次心跳
    sendHeartbeat()

    // 启动心跳定时器（每20秒）
    heartbeatTimer = setInterval(() => {
      sendHeartbeat()
    }, 20000)

    // 启动消息轮询
    pollMessages()
  }

  /**
   * 停止轮询
   */
  const stop = () => {
    isRunning = false
    
    if (pollTimer) {
      clearTimeout(pollTimer)
      pollTimer = null
    }
    
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }

    console.log('❌ HTTP长轮询已停止')
  }

  /**
   * 发送心跳
   */
  const sendHeartbeat = async () => {
    try {
      await request.post('/polling/heartbeat', { userId })
      console.log('💓 心跳发送成功')
    } catch (error) {
      console.error('心跳发送失败:', error)
    }
  }

  /**
   * 轮询消息
   */
  const pollMessages = async () => {
    if (!isRunning) return

    try {
      console.log('🔄 开始轮询消息...')
      
      const response = await request.post('/polling/messages', {
        userId,
        lastMessageId,
        timeout: POLL_SERVER_TIMEOUT_SEC
      }, {
        timeout: POLL_CLIENT_TIMEOUT_MS,
        // 长轮询正常结束也可能接近超时，不在全局拦截器弹窗
        skipErrorHandler: true
      })

      if (response.data && response.data.length > 0) {
        console.log(`📨 收到${response.data.length}条新消息`)
        
        response.data.forEach(message => {
          onMessage(message)
          
          // 更新最后消息ID
          if (message.id) {
            lastMessageId = message.id
          }
        })
      } else {
        console.log('⏰ 轮询超时，无新消息')
      }

      // 继续下一轮轮询
      if (isRunning) {
        pollMessages()
      }
    } catch (error) {
      console.error('消息轮询失败:', error)
      onError(error)

      // 出错后延迟重试
      if (isRunning) {
        pollTimer = setTimeout(() => {
          pollMessages()
        }, 3000)
      }
    }
  }

  /**
   * 发送消息
   */
  const sendMessage = async (messageData) => {
    try {
      const response = await request.post('/polling/send', messageData)
      console.log('✅ 消息发送成功')
      return response.data
    } catch (error) {
      console.error('消息发送失败:', error)
      throw error
    }
  }

  /**
   * 查询在线状态
   */
  const checkOnlineStatus = async (userIds) => {
    try {
      const response = await request.get('/polling/online-status', {
        params: { userIds: userIds.join(',') }
      })
      return response.data
    } catch (error) {
      console.error('查询在线状态失败:', error)
      return {}
    }
  }

  /**
   * 用户登出
   */
  const logout = async () => {
    try {
      await request.post('/polling/logout', { userId })
      console.log('✅ 用户登出成功')
    } catch (error) {
      console.error('登出失败:', error)
    }
  }

  return {
    start,
    stop,
    sendHeartbeat,
    sendMessage,
    checkOnlineStatus,
    logout
  }
}
