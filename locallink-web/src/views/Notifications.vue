<template>
  <div class="notifications-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>消息通知</h2>
          <el-button 
            v-if="notifications.length > 0" 
            type="primary" 
            size="small" 
            @click="markAllAsRead"
          >
            全部标记为已读
          </el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <!-- 系统通知 -->
        <el-tab-pane label="系统通知" name="system">
          <el-scrollbar style="height: calc(100vh - 280px)">
            <div v-if="systemNotifications.length" class="notification-list">
              <div 
                v-for="notif in systemNotifications" 
                :key="notif.id" 
                class="notification-item"
                :class="{ unread: !notif.isRead }"
                @click="handleNotificationClick(notif)"
              >
                <div class="notification-icon">
                  <el-icon :size="24" :color="getNotificationColor(notif.type)">
                    <component :is="getNotificationIcon(notif.type)" />
                  </el-icon>
                </div>
                <div class="notification-content">
                  <div class="notification-title">{{ notif.title }}</div>
                  <div class="notification-message">{{ notif.content }}</div>
                  <div class="notification-time">{{ formatTime(notif.createTime) }}</div>
                </div>
                <el-badge v-if="!notif.isRead" is-dot />
              </div>
            </div>
            <el-empty v-else description="暂无系统通知" />
          </el-scrollbar>
        </el-tab-pane>
        
        <!-- 好友申请 -->
        <el-tab-pane :label="`好友申请(${friendRequestNotifications.length})`" name="friend">
          <el-scrollbar style="height: calc(100vh - 280px)">
            <div v-if="friendRequestNotifications.length" class="notification-list">
              <div 
                v-for="request in friendRequestNotifications" 
                :key="request.id" 
                class="notification-item friend-request"
                :class="{ unread: !request.isRead }"
              >
                <div class="notification-icon">
                  <el-icon :size="24" color="#409eff">
                    <User />
                  </el-icon>
                </div>
                <div class="notification-content">
                  <div class="notification-title">
                    {{ request.title }}
                  </div>
                  <div class="notification-message">{{ request.content }}</div>
                  <div class="notification-time">{{ formatTime(request.createTime) }}</div>
                  <div class="notification-actions">
                    <el-button type="success" size="small" @click="handleAcceptFriend(request.relatedId)">接受</el-button>
                    <el-button type="danger" size="small" @click="handleRejectFriend(request.relatedId)">拒绝</el-button>
                  </div>
                </div>
                <el-badge v-if="!request.isRead" is-dot />
              </div>
            </div>
            <el-empty v-else description="暂无好友申请" />
          </el-scrollbar>
        </el-tab-pane>
        
        <!-- 处罚通知 -->
        <el-tab-pane :label="`处罚通知(${penaltyRequestNotifications.length})`" name="penalty">
          <el-scrollbar style="height: calc(100vh - 280px)">
            <div v-if="penaltyRequestNotifications.length" class="notification-list">
              <div 
                v-for="penalty in penaltyRequestNotifications" 
                :key="penalty.id" 
                class="notification-item penalty"
                :class="{ unread: !penalty.isRead }"
              >
                <div class="notification-icon">
                  <el-icon :size="24" color="#f56c6c">
                    <Warning />
                  </el-icon>
                </div>
                <div class="notification-content">
                  <div class="notification-title">{{ penalty.title }}</div>
                  <div class="notification-message">{{ penalty.content }}</div>
                  <div class="notification-time">{{ formatTime(penalty.createTime) }}</div>
                  <div class="notification-actions">
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="handleAppeal(penalty)"
                      v-if="!penalty.isRead || (penalty.isRead && !penalty.appealed)"
                    >
                      申诉
                    </el-button>
                  </div>
                </div>
                <el-badge v-if="!penalty.isRead" is-dot />
              </div>
            </div>
            <el-empty v-else description="暂无处罚通知" />
          </el-scrollbar>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Warning, CircleCheck, ChatDotRound, User } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { acceptFriend, rejectFriend } from '../api/friend'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead as markAllAsReadAPI } from '../api/notification'
import { submitAppeal } from '../api/admin'

const userStore = useUserStore()
const activeTab = ref('system')

// 通知数据
const notifications = ref([])
const friendRequests = ref([])
const penaltyNotifications = ref([])

const systemNotifications = computed(() => {
  return notifications.value.filter(n => n.type <= 3 || n.type === 7) // 系统通知、好友相关
})

// 好友申请通知
const friendRequestNotifications = computed(() => {
  return notifications.value.filter(n => n.type === 1 || n.type === 2) // 好友申请、好友通过
})

// 处罚通知
const penaltyRequestNotifications = computed(() => {
  return notifications.value.filter(n => n.type === 8) // 处罚通知
})

// 获取头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  const config = userStore.serverConfig
  const baseUrl = config?.baseUrl || 'http://localhost:8080'
  return avatar.startsWith('http') ? avatar : `${baseUrl}/${avatar}`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 获取通知图标
const getNotificationIcon = (type) => {
  const icons = {
    'info': Bell,
    'success': CircleCheck,
    'warning': Warning,
    'chat': ChatDotRound
  }
  return icons[type] || Bell
}

// 获取通知颜色
const getNotificationColor = (type) => {
  const colors = {
    'info': '#409eff',
    'success': '#67c23a',
    'warning': '#e6a23c',
    'chat': '#409eff'
  }
  return colors[type] || '#409eff'
}

// 点击通知
const handleNotificationClick = async (notif) => {
  if (!notif.isRead) {
    try {
      await markAsRead(notif.id)
      notif.isRead = 1
      // 刷新未读计数
      await loadUnreadCount()
      console.log('标记通知为已读:', notif.id)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  // 根据通知类型执行不同操作
  console.log('点击通知:', notif)
}

// 点击处罚通知 - 只标记为已读，不消失
const handlePenaltyClick = async (penalty) => {
  if (!penalty.isRead) {
    try {
      await markAsRead(penalty.id)
      penalty.isRead = 1
      // 刷新未读计数
      await loadUnreadCount()
      console.log('标记处罚通知为已读:', penalty.id)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  // 不再弹出对话框，只在列表中显示申诉按钮
}

// 处理申诉
const handleAppeal = async (penalty) => {
  try {
    const { value: content } = await ElMessageBox.prompt(
      '请输入申诉理由',
      '提交申诉',
      {
        inputType: 'textarea',
        inputPlaceholder: '请详细说明您的申诉理由...',
        inputValidator: (value) => {
          if (!value || !value.trim()) {
            return '申诉理由不能为空'
          }
          return true
        }
      }
    )
    
    if (content) {
      // 调用申诉API
      await submitAppeal(userStore.userInfo.id, `[申诉] ${content}`)
      ElMessage.success('申诉已提交，等待管理员处理')
      // 标记为已申诉，避免重复申诉
      penalty.appealed = true
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('申诉提交失败')
    }
  }
}

// 接受好友申请
const handleAcceptFriend = async (relationId) => {
  try {
    await acceptFriend(relationId)
    ElMessage.success('已接受好友申请')
    // 标记通知为已读
    await markAsRead(relationId)
    loadNotifications()
  } catch (error) {
    ElMessage.error('接受失败')
  }
}

// 拒绝好友申请
const handleRejectFriend = async (relationId) => {
  try {
    await rejectFriend(relationId)
    ElMessage.success('已拒绝好友申请')
    // 标记通知为已读
    await markAsRead(relationId)
    loadNotifications()
  } catch (error) {
    ElMessage.error('拒绝失败')
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  try {
    await markAllAsReadAPI(userStore.userInfo.id)
    notifications.value.forEach(n => n.isRead = 1)
    ElMessage.success('已全部标记为已读')
    // 刷新未读计数
    await loadUnreadCount()
    loadNotifications()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 加载通知数据
const loadNotifications = async () => {
  try {
    const res = await getNotifications(userStore.userInfo.id)
    notifications.value = res.data || []
    
    // 同步到各个分类数组（用于标签页计数）
    console.log('加载通知成功:', notifications.value.length, '条')
    console.log('系统通知:', systemNotifications.value.length, '条')
    console.log('好友申请:', friendRequestNotifications.value.length, '条')
    console.log('处罚通知:', penaltyRequestNotifications.value.length, '条')
  } catch (error) {
    console.error('加载通知失败:', error)
    ElMessage.error('加载通知失败')
  }
}

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount(userStore.userInfo.id)
    // 更新到 store
    userStore.userInfo.unreadNotifications = res.data || 0
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

onMounted(() => {
  loadNotifications()
  loadUnreadCount()
})
</script>

<style scoped>
.notifications-container {
  height: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px;
  border-radius: 8px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
}

.notification-item.unread {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.notification-item.penalty {
  border-left: 4px solid #f56c6c;
}

.notification-item.friend-request {
  cursor: default;
}

.notification-icon {
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.notification-message {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.5;
}

.notification-reason {
  font-size: 13px;
  color: #f56c6c;
  margin-bottom: 8px;
  padding: 8px;
  background-color: #fef0f0;
  border-radius: 4px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.appeal-hint {
  margin-top: 10px;
}
</style>
