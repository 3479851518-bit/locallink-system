<template>
  <div class="main-layout">
    <el-container>
      <el-aside width="200px">
        <el-menu :default-active="activeMenu" @select="handleMenuSelect">
          <!-- 管理员不显示好友和群组菜单 -->
          <template v-if="userStore.userInfo?.role !== USER_ROLE.ADMIN">
            <el-menu-item index="friends">
              <el-icon><User /></el-icon>
              <span>好友</span>
            </el-menu-item>
            <el-menu-item index="groups">
              <el-icon><UserFilled /></el-icon>
              <span>群组</span>
            </el-menu-item>
          </template>
          
          <el-menu-item index="chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ userStore.userInfo?.role === USER_ROLE.ADMIN ? '申诉处理' : '聊天' }}</span>
          </el-menu-item>
          
          <el-menu-item index="notifications">
            <el-icon><Bell /></el-icon>
            <span>通知</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" style="margin-left: 5px" />
          </el-menu-item>
          
          <el-menu-item index="profile">
            <el-icon><Avatar /></el-icon>
            <span>我的</span>
          </el-menu-item>
          <el-menu-item 
            v-if="userStore.userInfo?.role === USER_ROLE.ADMIN" 
            index="admin"
            style="color: #f56c6c"
          >
            <el-icon><Warning /></el-icon>
            <span>管理控制台</span>
          </el-menu-item>
        </el-menu>
        <div class="user-info">
          <div class="user-profile" @click="handleGoToProfile">
            <el-avatar :size="40" :src="getAvatarUrl()">
              {{ userStore.userInfo?.nickname?.[0] || '用户' }}
            </el-avatar>
            <div class="user-details">
              <div class="user-name">{{ userStore.userInfo?.nickname || '未设置昵称' }}</div>
              <div class="user-status-wrapper">
                <el-dropdown @command="handleStatusChange">
                  <span class="user-status" :class="'status-' + userStore.userInfo?.status">
                    {{ getStatusText(userStore.userInfo?.status) }}
                    <el-icon><ArrowDown /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="ONLINE_STATUS.OFFLINE">离线</el-dropdown-item>
                      <el-dropdown-item :command="ONLINE_STATUS.ONLINE">在线</el-dropdown-item>
                      <el-dropdown-item :command="ONLINE_STATUS.BUSY">忙碌</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { ChatDotRound, User, UserFilled, ArrowDown, Avatar, Warning, Bell } from '@element-plus/icons-vue'
import { updateStatus, getServerConfig } from '../api/user'
import { ElMessage } from 'element-plus'
import { USER_ROLE, ONLINE_STATUS, ONLINE_STATUS_TEXT } from '../constants'
import { createHttpPolling } from '../utils/httpPolling'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const httpPolling = ref(null) // HTTP轮询控制器
const heartbeatTimer = ref(null)
const heartbeatInterval = 20000 // 20秒发送一次心跳

const activeMenu = computed(() => route.name.toLowerCase())
const unreadCount = computed(() => {
  // 这里可以从 store 或 localStorage 获取未读通知数量
  return userStore.userInfo?.unreadNotifications || 0
})

// 获取头像URL
const getAvatarUrl = () => {
  const config = userStore.serverConfig
  const baseUrl = config?.baseUrl || 'http://localhost:8080'
  
  const avatar = userStore.userInfo?.avatar
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return `${baseUrl}/${avatar}`
}

const handleMenuSelect = (index) => {
  router.push(`/${index}`)
}

const handleLogout = () => {
  userStore.logout() // store 已经处理了 localStorage 清理
  router.push('/login')
}

// 跳转到个人中心
const handleGoToProfile = () => {
  router.push('/profile')
}

// 获取状态文本（使用常量）
const getStatusText = (status) => {
  return ONLINE_STATUS_TEXT[status] || ONLINE_STATUS_TEXT[ONLINE_STATUS.OFFLINE]
}

// 修改在线状态
const handleStatusChange = async (status) => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  try {
    await updateStatus({ userId: userStore.userInfo.id, status: parseInt(status) })
    // 更新本地用户信息
    userStore.userInfo.status = parseInt(status)
    ElMessage.success('状态已更新')
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 设置HTTP长轮询
const setupHttpPolling = async () => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    console.error('用户信息未加载，无法启动HTTP轮询')
    return
  }
  
  // 确保服务器配置已加载
  if (!userStore.serverConfig) {
    console.log('服务器配置未加载，正在获取...')
    try {
      const configRes = await getServerConfig()
      userStore.setServerConfig(configRes.data)
      console.log('服务器配置已加载:', configRes.data)
    } catch (error) {
      console.warn('获取服务器配置失败，将使用默认配置:', error)
      // 使用当前页面地址作为默认配置
      const httpProtocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
      const host = window.location.host
      userStore.setServerConfig({
        baseUrl: `${httpProtocol}//${host}`,
        serverHost: host.split(':')[0],
        serverPort: host.split(':')[1] || '80'
      })
    }
  }
  
  // 如果已有轮询，先停止
  if (httpPolling.value) {
    console.log('停止旧的HTTP轮询')
    httpPolling.value.stop()
    httpPolling.value = null
  }
  
  console.log('正在创建HTTP长轮询连接...')
  const polling = createHttpPolling(userStore.userInfo.id, {
    onMessage: (data) => {
      console.log('📨 收到消息:', data)
      
      // 处理通知消息
      if (data.type === 'notification') {
        // 增加未读数量
        if (userStore.userInfo.unreadNotifications === undefined) {
          userStore.userInfo.unreadNotifications = 0
        }
        userStore.userInfo.unreadNotifications++
        
        // 显示通知提示
        ElMessage({
          message: `${data.title}: ${data.content}`,
          type: 'info',
          duration: 3000
        })
      }
      // 处理聊天消息（由Chat.vue自行处理，这里只是透传）
      else if (data.senderId || data.groupId) {
        // 这是一个聊天消息，触发全局事件让Chat.vue接收
        window.dispatchEvent(new CustomEvent('new-chat-message', { detail: data }))
      }
    },
    onError: (error) => {
      console.error('⚠️ HTTP轮询错误:', error)
    }
  })
  
  httpPolling.value = polling
  
  // 启动轮询
  polling.start()
  
  // 存储到store中供其他组件使用
  userStore.setHttpPolling(polling)
}

// 页面加载时检查是否需要启动HTTP轮询
onMounted(() => {
  if (userStore.userInfo && userStore.userInfo.id) {
    console.log('初始化HTTP长轮询...')
    setupHttpPolling()
  }
})

onUnmounted(() => {
  // 停止HTTP轮询
  if (httpPolling.value) {
    httpPolling.value.stop()
    httpPolling.value = null
  }
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  width: 220px !important;
}

.el-menu {
  flex: 1;
  border-right: none;
}

.user-info {
  padding: 15px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex: 1;
  min-width: 0;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s;
}

.user-profile:hover {
  background-color: #f5f7fa;
}

.user-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-status-wrapper {
  /* 移除 flex: 1，让内容自适应 */
}

.user-status {
  cursor: pointer;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 0.3s;
  white-space: nowrap;
}

.user-status:hover {
  background-color: #f5f7fa;
}

.status-0 {
  color: #909399;
}

.status-1 {
  color: #67c23a;
}

.status-2 {
  color: #e6a23c;
}
</style>
