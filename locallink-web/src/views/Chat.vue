<template>
  <div class="chat-container">
    <el-row :gutter="20" style="height: 100%">
      <el-col :span="8">
        <el-card class="friend-list-card">
          <!-- 申诉状态提示：如果用户被禁言且正在申诉中，显示申诉状态 -->
          <div v-if="isMuteActive() && userStore.userInfo?.role !== USER_ROLE.ADMIN" class="appeal-status-banner">
            <el-icon color="#e6a23c"><Warning /></el-icon>
            <span>您正处于申诉处理中，聊天功能已限制</span>
          </div>
          
          <el-input v-model="searchKeyword" placeholder="搜索" clearable @input="handleSearch" style="margin-bottom: 10px" />
          <el-scrollbar style="height: calc(100vh - 200px)">
            <!-- 管理员显示申诉列表 -->
            <template v-if="userStore.userInfo?.role === USER_ROLE.ADMIN">
              <div class="section-title">申诉列表</div>
              <div
                v-for="appeal in appealList"
                :key="'appeal-' + appeal.id"
                class="chat-item"
                :class="{ active: currentChat?.type === 'appeal' && currentChat?.id === appeal.userId }"
                @click="selectAppealUser(appeal)"
              >
                <el-avatar :size="40" :src="getAvatarUrl({ senderId: appeal.userId, senderAvatar: appeal.avatar })">
                  {{ appeal.nickname?.[0] }}
                </el-avatar>
                <div class="chat-item-info">
                  <div class="chat-item-name">{{ appeal.nickname }}</div>
                  <div class="chat-item-desc" style="font-size: 12px; color: #909399">{{ appeal.account }}</div>
                </div>
              </div>
              <el-empty v-if="appealList.length === 0" description="暂无申诉" :image-size="80" />
            </template>
            
            <!-- 普通用户显示好友和群组 -->
            <template v-else>
              <!-- 好友列表 -->
              <div class="section-title">好友</div>
              <div
                v-for="friend in friendList"
                :key="'friend-' + friend.id"
                class="chat-item"
                :class="{ active: currentChat?.type === 'friend' && currentChat?.id === friend.id }"
                @click="selectFriend(friend)"
              >
                <el-avatar :size="40" :src="getFriendAvatarUrl(friend)">
                  {{ friend.nickname?.[0] }}
                </el-avatar>
                <div class="chat-item-info">
                  <div class="chat-item-name">{{ friend.nickname }}</div>
                  <div class="chat-item-status" :class="'status-' + friend.status">
                    {{ ONLINE_STATUS_TEXT[friend.status] || ONLINE_STATUS_TEXT[ONLINE_STATUS.OFFLINE] }}
                  </div>
                </div>
              </div>
              
              <!-- 群组列表 -->
              <div class="section-title" style="margin-top: 20px;">群组</div>
              <div
                v-for="group in groupList"
                :key="'group-' + group.id"
                class="chat-item"
                :class="{ active: currentChat?.type === 'group' && currentChat?.id === group.id }"
                @click="selectGroup(group)"
              >
                <el-avatar shape="square" :size="40" :src="getGroupAvatarUrl(group)">
                  {{ group.groupName?.[0] }}
                </el-avatar>
                <div class="chat-item-info">
                  <div class="chat-item-name">{{ group.groupName }}</div>
                  <div class="chat-item-desc">群聊</div>
                </div>
              </div>
            </template>
          </el-scrollbar>
        </el-card>
      </el-col>
      <el-col :span="16">
        <!-- 申诉期间隐藏聊天界面 -->
        <el-empty v-if="isMuteActive() && userStore.userInfo?.role !== USER_ROLE.ADMIN" description="您已被禁言，正在申诉处理中。请前往【消息通知】查看申诉进度和原因。" :image-size="150">
          <template #image>
            <el-icon :size="100" color="#f56c6c"><Lock /></el-icon>
          </template>
        </el-empty>
        
        <el-card class="chat-card" v-else-if="currentChat">
          <template #header>
            <div class="chat-header">
              <!-- 左侧：聊天标题 -->
              <div class="chat-title-section">
                <el-tag v-if="currentChat.type === 'group'" type="success" size="small" style="margin-right: 8px">群聊</el-tag>
                <span class="chat-name">{{ currentChat.name }}</span>
              </div>
              
              <!-- 右侧：状态和统计信息 -->
              <div class="chat-header-right">
                <!-- 群聊统计信息 -->
                <div v-if="currentChat.type === 'group'" class="group-stats">
                  <el-tooltip content="群成员总数" placement="top">
                    <el-tag size="small" type="info">
                      <el-icon><User /></el-icon>
                      {{ groupMembers.length }}
                    </el-tag>
                  </el-tooltip>
                  <el-tooltip content="在线成员" placement="top">
                    <el-tag size="small" type="success">
                      <el-icon><VideoCamera /></el-icon>
                      {{ onlineMembers.length }}
                    </el-tag>
                  </el-tooltip>
                  <el-button 
                    type="primary" 
                    link 
                    size="small" 
                    @click="showMemberDialog = true"
                  >
                    查看成员
                  </el-button>
                </div>
              </div>
            </div>
          </template>
          <el-scrollbar ref="messageScroll" style="height: calc(100vh - 300px)">
            <div 
              v-for="msg in messages" 
              :key="msg.id" 
              class="message-item" 
              :class="[
                msg.senderId === userStore.userInfo?.id ? 'my-message' : 'other-message'
              ]"
              @contextmenu.prevent="showMessageContextMenu($event, msg)"
            >
              <!-- 显示头像 -->
              <el-avatar :size="36" :src="getAvatarUrl(msg)">
                {{ getAvatarText(msg) }}
              </el-avatar>
              <div class="message-content">
                <div v-if="currentChat?.type === 'group' && msg.senderId !== userStore.userInfo?.id" class="sender-name">
                  {{ msg.senderNickname || '未知用户' }}
                </div>
                <div class="message-text" :class="{ recalled: msg.isRecalled === 1 }">
                  {{ formatMessageContent(msg) }}
                </div>
                <div class="message-time">
                  {{ formatTime(msg.sendTime) }}
                  <el-button 
                    v-if="msg.senderId === userStore.userInfo?.id && msg.isRecalled !== 1 && canRecall(msg)" 
                    type="text" 
                    size="small" 
                    @click="handleRecallMessage(msg)"
                    style="margin-left: 8px; color: #f56c6c"
                  >
                    撤回
                  </el-button>
                </div>
              </div>
            </div>
          </el-scrollbar>
          <!-- 禁言提示 -->
          <div v-if="userStore.userInfo?.isMuted === 1" class="mute-hint">
            <el-alert
              type="warning"
              :closable="false"
              show-icon
            >
              <template #title>
                <span v-if="userStore.userInfo.muteExpireTime">
                  您已被禁言，剩余时间：{{ getRemainingMuteTime() }}。原因：{{ userStore.userInfo.muteReason || '未知' }}
                </span>
                <span v-else>
                  您已被永久禁言。原因：{{ userStore.userInfo.muteReason || '未知' }}
                </span>
              </template>
            </el-alert>
          </div>
          <div class="message-input">
            <el-input 
              v-model="inputMessage" 
              placeholder="输入消息..." 
              @keyup.enter="sendMessage"
              :disabled="userStore.userInfo?.isMuted === 1 && isMuteActive() && userStore.userInfo?.role !== USER_ROLE.ADMIN"
            />
            <el-button 
              type="primary" 
              @click="sendMessage" 
              style="margin-left: 10px"
              :disabled="userStore.userInfo?.isMuted === 1 && isMuteActive() && userStore.userInfo?.role !== USER_ROLE.ADMIN"
            >
              发送
            </el-button>
          </div>
        </el-card>
        <el-empty v-else :description="userStore.userInfo?.role === USER_ROLE.ADMIN ? '选择一个申诉用户开始处理' : activeTab === 'friend' ? '选择一个好友开始聊天' : '选择一个群组开始聊天'" />
      </el-col>
    </el-row>
    
    <!-- 群成员对话框 -->
    <el-dialog 
      v-model="showMemberDialog" 
      :title="`${currentChat?.name} - 群成员`" 
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="member-stats">
        <el-tag type="info" size="large">
          <el-icon><User /></el-icon>
          总成员：{{ groupMembers.length }}
        </el-tag>
        <el-tag type="success" size="large" style="margin-left: 10px">
          <el-icon><VideoCamera /></el-icon>
          在线：{{ onlineMembers.length }}
        </el-tag>
      </div>
      
      <el-divider />
      
      <el-scrollbar style="max-height: 400px">
        <div v-for="member in groupMembers" :key="member.userId" class="member-item">
          <el-avatar :size="40" :src="getMemberAvatarUrl(member)">
            {{ member.nickname?.[0] || '?' }}
          </el-avatar>
          <div class="member-info">
            <div class="member-name">
              {{ member.nickname || '未知用户' }}
              <el-tag v-if="member.role === GROUP_ROLE.OWNER" type="warning" size="small" style="margin-left: 5px">群主</el-tag>
              <el-tag v-else-if="member.role === GROUP_ROLE.ADMIN" type="success" size="small" style="margin-left: 5px">管理员</el-tag>
            </div>
            <div class="member-status" :class="'status-' + member.status">
              {{ member.status === ONLINE_STATUS.ONLINE ? '● 在线' : member.status === ONLINE_STATUS.BUSY ? '● 忙碌' : '○ 离线' }}
            </div>
          </div>
        </div>
      </el-scrollbar>
      
      <template #footer>
        <el-button @click="showMemberDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 消息右键菜单 -->
    <div 
      v-show="messageContextMenuVisible" 
      class="message-context-menu"
      :style="{
        left: messageContextMenuPosition.x + 'px',
        top: messageContextMenuPosition.y + 'px'
      }"
    >
      <div class="context-menu-item" @click="handleCopyMessage">
        <el-icon><DocumentCopy /></el-icon>
        <span>复制</span>
      </div>
      <div class="context-menu-item" @click="handleForwardMessage">
        <el-icon><Promotion /></el-icon>
        <span>转发</span>
      </div>
      <div class="context-menu-item" @click="handleQuoteMessage">
        <el-icon><ChatDotRound /></el-icon>
        <span>引用</span>
      </div>
      <div class="context-menu-divider"></div>
      <div 
        v-if="currentContextMenuMessage?.senderId === userStore.userInfo?.id && currentContextMenuMessage?.isRecalled !== 1" 
        class="context-menu-item danger" 
        @click="handleDeleteMessage"
      >
        <el-icon><Delete /></el-icon>
        <span>删除</span>
      </div>
      <div 
        v-if="currentContextMenuMessage?.senderId !== userStore.userInfo?.id" 
        class="context-menu-item warning" 
        @click="handleReportFromMenu"
      >
        <el-icon><Warning /></el-icon>
        <span>举报</span>
      </div>
    </div>

    <!-- 转发对话框 -->
    <el-dialog v-model="showForwardDialog" title="转发消息" width="500px">
      <el-tabs v-model="forwardTab">
        <el-tab-pane label="好友" name="friend">
          <el-scrollbar style="max-height: 300px">
            <div v-for="friend in friendList" :key="friend.id" class="forward-target-item" @click="selectForwardTarget(friend, 'friend')">
              <el-avatar :size="36" :src="getFriendAvatarUrl(friend)">{{ friend.nickname?.[0] }}</el-avatar>
              <span class="forward-target-name">{{ friend.nickname }}</span>
              <el-icon v-if="forwardTarget?.id === friend.id && forwardTarget?.type === 'friend'" color="#409eff"><Check /></el-icon>
            </div>
          </el-scrollbar>
        </el-tab-pane>
        <el-tab-pane label="群组" name="group">
          <el-scrollbar style="max-height: 300px">
            <div v-for="group in groupList" :key="group.id" class="forward-target-item" @click="selectForwardTarget(group, 'group')">
              <el-avatar shape="square" :size="36" :src="getGroupAvatarUrl(group)">{{ group.groupName?.[0] }}</el-avatar>
              <span class="forward-target-name">{{ group.groupName }}</span>
              <el-icon v-if="forwardTarget?.id === group.id && forwardTarget?.type === 'group'" color="#409eff"><Check /></el-icon>
            </div>
          </el-scrollbar>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showForwardDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmForward" :disabled="!forwardTarget">确定转发</el-button>
      </template>
    </el-dialog>

    <!-- 引用消息提示 -->
    <div v-if="quotedMessage" class="quoted-message-bar">
      <el-tag size="small" type="info" closable @close="cancelQuote">
        引用: {{ quotedMessage.content?.substring(0, 50) }}{{ quotedMessage.content?.length > 50 ? '...' : '' }}
      </el-tag>
    </div>

    <!-- 举报对话框 -->
    <el-dialog v-model="showReportDialogVisible" title="举报消息" width="500px">
      <el-form :model="reportForm" label-width="100px">
        <el-form-item label="举报对象">
          <span>{{ currentReportMessage?.senderNickname || '未知用户' }}</span>
        </el-form-item>
        <el-form-item label="消息内容">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px; max-height: 100px; overflow-y: auto">
            {{ currentReportMessage?.content }}
          </div>
        </el-form-item>
        <el-form-item label="举报原因" required>
          <el-select 
            v-model="reportForm.reasonCode" 
            placeholder="请选择举报原因"
            style="width: 100%"
          >
            <el-option
              v-for="item in reportReasons"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明">
          <el-input 
            v-model="reportForm.reason" 
            type="textarea" 
            :rows="3"
            placeholder="请详细描述举报情况（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="聊天记录">
          <el-checkbox v-model="reportForm.includeMessages" :true-label="1" :false-label="0">
            包含最近的聊天记录（有助于管理员核实）
          </el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReportDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleSubmitReport">提交举报</el-button>
      </template>
    </el-dialog>
    
    <!-- 处罚通知对话框 -->
    <el-dialog v-model="showPenaltyNotificationVisible" :title="currentPenaltyNotification?.title || '处罚通知'" width="500px">
      <div class="penalty-notification">
        <el-alert
          :title="currentPenaltyNotification?.title || '通知'"
          :type="currentPenaltyNotification?.title?.includes('申诉') ? 'info' : 'error'"
          :closable="false"
          show-icon
        />
        <div class="penalty-content">
          <p>{{ currentPenaltyNotification?.content }}</p>
          <div v-if="currentPenaltyNotification?.reason" class="penalty-reason">
            <strong>原因：</strong>{{ currentPenaltyNotification.reason }}
          </div>
          <div class="penalty-time">
            <strong>时间：</strong>{{ formatNotificationTime(currentPenaltyNotification?.timestamp) }}
          </div>
        </div>
        <div v-if="currentPenaltyNotification?.canAppeal" class="appeal-hint">
          <el-icon><InfoFilled /></el-icon>
          <span>如您对此有异议，可以点击“申诉”按钮向管理员提出申诉</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPenaltyNotificationVisible = false">关闭</el-button>
        <el-button v-if="currentPenaltyNotification?.canAppeal && !currentPenaltyNotification?.title?.includes('申诉')" type="primary" @click="handleAppealFromNotification">申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getFriendList } from '../api/friend'
import { getUserGroups, getGroupMembers } from '../api/group'
import { getHistory, getGroupHistory, recallMessage, deleteMessage } from '../api/message'
import { submitReport, getPendingAppeals, submitAppeal } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, VideoCamera, DocumentCopy, Promotion, ChatDotRound, Delete, Warning, Check, InfoFilled, Lock, Connection, Loading } from '@element-plus/icons-vue'
import { 
  USER_ROLE, 
  ONLINE_STATUS, 
  ONLINE_STATUS_TEXT,
  CHAT_TYPE,
  MESSAGE_TYPE,
  GROUP_ROLE,
  REPORT_REASON,
  REPORT_REASON_TEXT
} from '../constants'
import { safeSend } from '../utils/websocket'
import { showErrorOnce } from '../utils/message'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const friendList = ref([])
const groupList = ref([])
const appealList = ref([]) // 管理员申诉列表
const searchKeyword = ref('')
const currentChat = ref(null) // { type: 'friend'/'group'/'appeal', id, name }
const messages = ref([])
const inputMessage = ref('')
const messageScroll = ref(null)
const groupMembers = ref([])
const showMemberDialog = ref(false)
const memberRefreshTimer = ref(null)

// 消息右键菜单相关
const messageContextMenuVisible = ref(false)
const messageContextMenuPosition = ref({ x: 0, y: 0 })
const currentContextMenuMessage = ref(null)

// 转发相关
const showForwardDialog = ref(false)
const forwardTab = ref('friend')
const forwardTarget = ref(null)

// 引用相关
const quotedMessage = ref(null)



// 举报相关
const showReportDialogVisible = ref(false)
const reportForm = ref({
  reasonCode: null,
  reason: '',
  includeMessages: 0
})
const currentReportMessage = ref(null)

// 处罚通知相关
const showPenaltyNotificationVisible = ref(false)
const currentPenaltyNotification = ref(null)

// 举报原因选项
const reportReasons = [
  { value: 1, label: '发布垃圾广告' },
  { value: 2, label: '骚扰他人' },
  { value: 3, label: '欺诈行为' },
  { value: 4, label: '不当内容' },
  { value: 5, label: '冒充他人' },
  { value: 6, label: '侵犯隐私' },
  { value: 7, label: '其他原因' }
]

const loadFriends = async () => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  try {
    const res = await getFriendList(userStore.userInfo.id)
    friendList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadGroups = async () => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  try {
    const res = await getUserGroups(userStore.userInfo.id)
    groupList.value = res.data || []
  } catch (error) {
    console.error('加载群组列表失败:', error)
    groupList.value = []
  }
}

// 加载申诉列表（仅管理员）
const loadAppeals = async () => {
  if (userStore.userInfo?.role !== 1) return
  
  try {
    const res = await getPendingAppeals()
    appealList.value = res.data || []
    console.log('加载申诉列表:', appealList.value.length, '条')
  } catch (error) {
    console.error('加载申诉列表失败:', error)
    appealList.value = []
  }
}

const selectFriend = async (friend) => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  currentChat.value = {
    type: 'friend',
    id: friend.id,
    name: friend.nickname
  }
  
  try {
    const res = await getHistory({
      userId: userStore.userInfo.id,
      friendId: friend.id,
      limit: 50
    })
    // 后端返回的已包含 senderNickname 和 senderAvatar
    messages.value = res.data.reverse()
    console.log('加载私聊历史消息:', messages.value.length, '条')
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error(error)
  }
}

const selectGroup = async (group) => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  currentChat.value = {
    type: 'group',
    id: group.id,
    name: group.groupName,
    groupId: group.groupId // 确保使用正确的 groupId
  }
  
  console.log('选择群组:', group)
  console.log('当前聊天对象:', currentChat.value)
  
  // 加载群聊历史消息（带上userId进行权限验证）
  try {
    const res = await getGroupHistory(group.groupId, 50, userStore.userInfo.id)
    messages.value = (res.data || []).reverse()
    console.log('加载群聊历史消息:', messages.value.length, '条')
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('加载群聊历史消息失败:', error)
    // 如果是权限错误，显示友好提示（只弹一次）
    if (error.response?.data?.message?.includes('不是该群成员')) {
      showErrorOnce('您不是该群成员或群组不存在')
      messages.value = []
    } else {
      messages.value = []
    }
  }
  
  // 加载群成员列表
  await loadGroupMembers(group.groupId)
  
  // 启动定时刷新在线状态
  startMemberStatusRefresh()
}

// 选择申诉用户（管理员）
const selectAppealUser = async (appeal) => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  currentChat.value = {
    type: 'appeal',
    id: appeal.userId,
    name: appeal.nickname,
    avatar: appeal.avatar
  }
  
  console.log('选择申诉用户:', appeal)
  
  // 加载与该用户的聊天记录
  try {
    const res = await getHistory({
      userId: userStore.userInfo.id,
      friendId: appeal.userId,
      limit: 50
    })
    messages.value = res.data.reverse()
    console.log('加载申诉聊天记录:', messages.value.length, '条')
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('加载聊天记录失败:', error)
    messages.value = []
  }
}

// 显示消息右键菜单
const showMessageContextMenu = (event, msg) => {
  event.preventDefault()
  currentContextMenuMessage.value = msg
  messageContextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
  messageContextMenuVisible.value = true
}

// 关闭消息右键菜单
const closeMessageContextMenu = () => {
  messageContextMenuVisible.value = false
  currentContextMenuMessage.value = null
}

// 复制消息
const handleCopyMessage = async () => {
  if (!currentContextMenuMessage.value) return
  
  try {
    await navigator.clipboard.writeText(currentContextMenuMessage.value.content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = currentContextMenuMessage.value.content
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('已复制到剪贴板')
  }
  closeMessageContextMenu()
}

// 转发消息
const handleForwardMessage = () => {
  if (!currentContextMenuMessage.value) return
  // 单条转发
  currentForwardContent.value = currentContextMenuMessage.value.content
  showForwardDialog.value = true
  forwardTarget.value = null
  closeMessageContextMenu()
}

// 选择转发目标
const selectForwardTarget = (target, type) => {
  forwardTarget.value = {
    id: target.id || target.groupId,
    type: type,
    name: type === 'friend' ? target.nickname : target.groupName
  }
}

// 确认转发
const confirmForward = async () => {
  if (!forwardTarget.value || !currentForwardContent.value) return
  
  try {
    const message = {
      senderId: userStore.userInfo.id,
      chatType: forwardTarget.value.type === 'group' ? CHAT_TYPE.GROUP : CHAT_TYPE.PRIVATE,
      messageType: MESSAGE_TYPE.TEXT,
      content: currentForwardContent.value
    }
    
    if (forwardTarget.value.type === 'friend') {
      message.receiverId = forwardTarget.value.id
    } else {
      message.groupId = forwardTarget.value.id
    }
    
    // 使用 safeSend 工具函数
    safeSend(userStore.ws, {
      type: 'chat',
      ...message
    })
    
    ElMessage.success(`已转发给${forwardTarget.value.name}`)
    showForwardDialog.value = false
    forwardTarget.value = null
    currentForwardContent.value = ''
  } catch (error) {
    console.error('转发失败:', error)
    ElMessage.error('转发失败')
  }
}

// 引用消息
const handleQuoteMessage = () => {
  if (!currentContextMenuMessage.value) return
  quotedMessage.value = currentContextMenuMessage.value
  closeMessageContextMenu()
  ElMessage.info('已引用消息，请输入回复内容')
}

// 取消引用
const cancelQuote = () => {
  quotedMessage.value = null
}

// 删除消息
const handleDeleteMessage = async () => {
  if (!currentContextMenuMessage.value) return
  
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      type: 'warning'
    })
    
    // 调用后端删除接口
    await deleteMessage(currentContextMenuMessage.value.id, userStore.userInfo.id)
    
    // 从本地删除
    const index = messages.value.findIndex(m => m.id === currentContextMenuMessage.value.id)
    if (index !== -1) {
      messages.value.splice(index, 1)
    }
    
    ElMessage.success('消息已删除')
    closeMessageContextMenu()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 从菜单举报
const handleReportFromMenu = () => {
  if (!currentContextMenuMessage.value) return
  
  // 检查是否举报自己
  if (currentContextMenuMessage.value.senderId === userStore.userInfo.id) {
    ElMessage.warning('不能举报自己的消息')
    closeMessageContextMenu()
    return
  }
  
  currentReportMessage.value = currentContextMenuMessage.value
  reportForm.value = {
    reasonCode: null,
    reason: '',
    includeMessages: 1
  }
  showReportDialogVisible.value = true
  closeMessageContextMenu()
}



// 发送消息（支持引用）
const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  // 检查HTTP轮询是否启动
  if (!userStore.httpPolling) {
    ElMessage.error('消息服务未启动，请刷新页面')
    return
  }
  
  if (!currentChat.value) {
    ElMessage.warning('请先选择聊天对象')
    return
  }
  
  // 检查用户是否被禁言（管理员不受禁言限制）
  if (userStore.userInfo.role !== USER_ROLE.ADMIN && userStore.userInfo.isMuted === 1) {
    // 检查禁言是否过期
    if (userStore.userInfo.muteExpireTime) {
      const expireTime = new Date(userStore.userInfo.muteExpireTime)
      const now = new Date()
      if (expireTime > now) {
        // 还在禁言期内
        const remainingMinutes = Math.ceil((expireTime - now) / 1000 / 60)
        ElMessage.warning(`您已被禁言，剩余时间：${remainingMinutes}分钟。原因：${userStore.userInfo.muteReason || '未知'}`)
        return
      }
    } else {
      // 永久禁言
      ElMessage.warning(`您已被永久禁言。原因：${userStore.userInfo.muteReason || '未知'}`)
      return
    }
  }
  
  try {
    let content = inputMessage.value
    
    // 如果有引用消息，添加引用信息
    if (quotedMessage.value) {
      content = `[回复 @${quotedMessage.value.senderNickname || '未知用户'}] ${content}`
    }
    
    // 如果是普通用户提交申诉，添加[申诉]前缀
    // 管理员回复申诉不需要加前缀
    if (currentChat.value.type === 'appeal' && userStore.userInfo.role !== USER_ROLE.ADMIN) {
      content = `[申诉] ${content}`
    }
    
    const messageData = {
      senderId: userStore.userInfo.id,
      chatType: currentChat.value.type === 'group' ? CHAT_TYPE.GROUP : CHAT_TYPE.PRIVATE,
      messageType: MESSAGE_TYPE.TEXT,
      content: content
    }
    
    // 根据聊天类型设置接收者
    if (currentChat.value.type === 'friend' || currentChat.value.type === 'appeal') {
      messageData.receiverId = currentChat.value.id
    } else {
      messageData.groupId = currentChat.value.groupId
    }
    
    console.log('准备发送消息:', messageData)
    
    // 通过HTTP发送消息
    await userStore.httpPolling.sendMessage(messageData)
    
    console.log('✅ 消息发送成功')
    
    // 清空输入和引用
    inputMessage.value = ''
    quotedMessage.value = null
    
    ElMessage.success('消息已发送')
  } catch (error) {
    console.error('发送消息异常:', error)
    ElMessage.error('发送失败，请重试')
  }
}

const scrollToBottom = () => {
  const scrollbar = messageScroll.value?.wrapRef
  if (scrollbar) {
    scrollbar.scrollTop = scrollbar.scrollHeight
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 格式化消息内容，去掉重复的[申诉]前缀
const formatMessageContent = (msg) => {
  if (!msg.content) return ''
  
  // 如果是申诉聊天，去掉消息内容中的[申诉]前缀
  if (currentChat.value?.type === 'appeal') {
    return msg.content.replace(/^\[申诉\]\s*/g, '')
  }
  
  return msg.content
}

const getAvatarText = (msg) => {
  if (msg.senderId === userStore.userInfo?.id) {
    return '我'
  }
  
  // 群聊时显示发送者昵称的第一个字
  if (currentChat.value?.type === 'group') {
    // 优先使用消息中的 senderNickname，如果没有则使用 senderId
    const nickname = msg.senderNickname
    if (nickname) {
      return nickname.charAt(0)
    }
    // 兜底方案：从好友列表中查找
    const friend = friendList.value.find(f => f.id === msg.senderId)
    if (friend && friend.nickname) {
      return friend.nickname.charAt(0)
    }
    return '?'
  }
  
  // 申诉聊天：显示发送者昵称的第一个字
  if (currentChat.value?.type === 'appeal') {
    // 如果是管理员查看申诉，显示申诉用户的昵称
    if (userStore.userInfo?.role === USER_ROLE.ADMIN) {
      return currentChat.value?.name?.[0] || '?'
    }
    // 如果是用户查看，显示管理员的昵称
    return '管'
  }
  
  // 私聊显示好友昵称的第一个字
  return currentChat.value?.name?.[0] || '?'
}

// 获取头像URL
const getAvatarUrl = (msg) => {
  const config = userStore.serverConfig
  const baseUrl = config?.baseUrl || 'http://localhost:8080'
  
  // 如果是自己的消息，使用当前用户的头像
  if (msg.senderId === userStore.userInfo?.id) {
    const avatar = userStore.userInfo.avatar
    if (avatar) {
      return avatar.startsWith('http') ? avatar : `${baseUrl}/${avatar}`
    }
    return ''
  }
  
  // 申诉聊天：使用当前聊天对象的头像
  if (currentChat.value?.type === 'appeal') {
    // 如果是管理员查看申诉，显示申诉用户的头像
    if (userStore.userInfo?.role === USER_ROLE.ADMIN && currentChat.value?.avatar) {
      return currentChat.value.avatar.startsWith('http') ? currentChat.value.avatar : `${baseUrl}/${currentChat.value.avatar}`
    }
    // 如果是用户查看，不显示管理员头像（返回空）
    return ''
  }
  
  // 群聊或私聊，尝试从消息中获取发送者头像
  if (msg.senderAvatar) {
    return msg.senderAvatar.startsWith('http') ? msg.senderAvatar : `${baseUrl}/${msg.senderAvatar}`
  }
  
  // 如果没有头像，返回空字符串，el-avatar会显示文字
  return ''
}

// 获取好友头像URL
const getFriendAvatarUrl = (friend) => {
  const config = userStore.serverConfig
  const baseUrl = config?.baseUrl || 'http://localhost:8080'
  
  if (friend.avatar) {
    return friend.avatar.startsWith('http') ? friend.avatar : `${baseUrl}/${friend.avatar}`
  }
  return ''
}

// 获取群组头像URL
const getGroupAvatarUrl = (group) => {
  const config = userStore.serverConfig
  const baseUrl = config?.baseUrl || 'http://localhost:8080'
  
  if (group.groupAvatar) {
    return group.groupAvatar.startsWith('http') ? group.groupAvatar : `${baseUrl}/${group.groupAvatar}`
  }
  return ''
}

// 获取群成员头像URL
const getMemberAvatarUrl = (member) => {
  const config = userStore.serverConfig
  const baseUrl = config?.baseUrl || 'http://localhost:8080'
  
  if (member.avatar) {
    return member.avatar.startsWith('http') ? member.avatar : `${baseUrl}/${member.avatar}`
  }
  return ''
}

// 加载群成员列表
const loadGroupMembers = async (groupId) => {
  try {
    const res = await getGroupMembers(groupId)
    groupMembers.value = res.data || []
    console.log('群成员列表:', groupMembers.value)
  } catch (error) {
    console.error('加载群成员列表失败:', error)
    groupMembers.value = []
  }
}

// 计算在线成员
const onlineMembers = computed(() => {
  return groupMembers.value.filter(m => m.status === 1 || m.status === 2)
})



// 启动成员状态刷新定时器
const startMemberStatusRefresh = () => {
  // 清除之前的定时器
  if (memberRefreshTimer.value) {
    clearInterval(memberRefreshTimer.value)
  }
  
  // 每5秒刷新一次成员状态
  memberRefreshTimer.value = setInterval(() => {
    if (currentChat.value?.groupId) {
      loadGroupMembers(currentChat.value.groupId)
    }
  }, 5000)
}

// 停止成员状态刷新
const stopMemberStatusRefresh = () => {
  if (memberRefreshTimer.value) {
    clearInterval(memberRefreshTimer.value)
    memberRefreshTimer.value = null
  }
}

const handleSearch = () => {
  // TODO: 实现搜索功能
}

// 判断是否可以撤回消息（2分钟内）
const canRecall = (msg) => {
  if (!msg.sendTime) return false
  const sendTime = new Date(msg.sendTime)
  const now = new Date()
  const diffMinutes = (now - sendTime) / 1000 / 60
  return diffMinutes <= 2
}

// 撤回消息
const handleRecallMessage = async (msg) => {
  try {
    await ElMessageBox.confirm('确定要撤回这条消息吗？', '提示')
    
    if (!userStore.userInfo || !userStore.userInfo.id) {
      ElMessage.error('用户信息未加载')
      return
    }
    
    await recallMessage(msg.id, userStore.userInfo.id)
    ElMessage.success('消息已撤回')
    
    // 更新本地消息列表
    const targetMsg = messages.value.find(m => m.id === msg.id)
    if (targetMsg) {
      targetMsg.isRecalled = 1
      targetMsg.content = '[消息已撤回]'
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回消息失败:', error)
      ElMessage.error(error.message || '撤回失败')
    }
  }
}

// 显示举报菜单（保留用于兼容）
const showReportMenu = (event, msg) => {
  showMessageContextMenu(event, msg)
}

// 提交举报
const handleSubmitReport = async () => {
  if (!currentReportMessage.value) return
  if (!reportForm.value.reasonCode) {
    ElMessage.warning('请选择举报原因')
    return
  }
  
  try {
    const reportData = {
      reporterId: userStore.userInfo.id,
      reportType: 2, // 消息举报
      messageId: currentReportMessage.value.id,
      reasonCode: reportForm.value.reasonCode,
      reason: reportForm.value.reason,
      includeMessages: reportForm.value.includeMessages
    }
    
    // 如果是群聊，添加被举报用户ID
    if (currentChat.value?.type === 'group') {
      reportData.reportedUserId = currentReportMessage.value.senderId
    }
    
    await submitReport(reportData)
    ElMessage.success('举报已提交，管理员将尽快处理')
    showReportDialogVisible.value = false
  } catch (error) {
    console.error('提交举报失败:', error)
  }
}

// 格式化通知时间
const formatNotificationTime = (timestamp) => {
  if (!timestamp) return ''
  try {
    const date = new Date(timestamp)
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return timestamp
  }
}

// 检查禁言是否仍然有效
const isMuteActive = () => {
  if (!userStore.userInfo?.isMuted || userStore.userInfo.isMuted !== 1) {
    return false
  }
  
  // 如果没有过期时间，说明是永久禁言
  if (!userStore.userInfo.muteExpireTime) {
    return true
  }
  
  // 检查是否已过期
  const expireTime = new Date(userStore.userInfo.muteExpireTime)
  const now = new Date()
  
  // 如果已过期，返回 false（不应该显示禁言提示）
  if (expireTime <= now) {
    return false
  }
  
  return true
}

// 获取剩余禁言时间
const getRemainingMuteTime = () => {
  if (!userStore.userInfo?.muteExpireTime) {
    return '永久'
  }
  
  const expireTime = new Date(userStore.userInfo.muteExpireTime)
  const now = new Date()
  
  // 如果已过期，不应该调用此函数（isMuteActive 会返回 false）
  // 但为了安全，返回空字符串
  if (expireTime <= now) {
    return ''
  }
  
  const diffMs = expireTime - now
  const diffMinutes = Math.ceil(diffMs / 1000 / 60)
  
  if (diffMinutes < 60) {
    return `${diffMinutes}分钟`
  } else {
    const diffHours = Math.ceil(diffMinutes / 60)
    if (diffHours < 24) {
      return `${diffHours}小时`
    } else {
      const diffDays = Math.ceil(diffHours / 24)
      return `${diffDays}天`
    }
  }
}

// 从通知中申诉
const handleAppealFromNotification = async () => {
  try {
    const { value: content } = await ElMessageBox.prompt(
      '请输入申诉理由',
      '申诉',
      {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '申诉理由不能为空',
        inputType: 'textarea',
        inputPlaceholder: '请详细说明您认为处罚不当的原因...'
      }
    )
    
    if (content) {
      await submitAppeal(userStore.userInfo.id, `[申诉] ${content}`)
      ElMessage.success('申诉已提交，管理员将尽快处理')
      showPenaltyNotificationVisible.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交申诉失败:', error)
    }
  }
}

onMounted(async () => {
  // 管理员加载申诉列表，普通用户加载好友和群组
  if (userStore.userInfo?.role === 1) {
    loadAppeals()
  } else {
    await loadFriends()
    await loadGroups() // 等待群组列表加载完成
  }
  
  // 检查URL参数，如果有groupId则自动选择该群组
  const { groupId, groupName, type } = route.query
  if (type === 'group' && groupId) {
    console.log('从 URL 参数加载群组:', { groupId, groupName })
    // 群组列表已加载完成，直接查找
    const targetGroup = groupList.value.find(g => g.groupId === groupId)
    if (targetGroup) {
      console.log('找到目标群组，自动选择:', targetGroup)
      selectGroup(targetGroup)
    } else {
      console.warn('未找到目标群组:', groupId)
      ElMessage.warning('群组不存在或您不是群成员')
    }
  }
  
  // 监听新消息事件（来自主组件的HTTP轮询）
  const handleNewMessage = (event) => {
    const data = event.detail
    console.log('收到新消息事件:', data)
    
    // 判断是否是当前聊天的消息
    const isCurrentChat = currentChat.value && (
      (currentChat.value.type === 'friend' && 
       ((data.senderId === currentChat.value.id) || (data.receiverId === currentChat.value.id))) ||
      (currentChat.value.type === 'group' && data.groupId === currentChat.value.groupId) ||
      (currentChat.value.type === 'appeal' && 
       ((data.senderId === currentChat.value.id) || (data.receiverId === currentChat.value.id)))
    )
    
    if (isCurrentChat) {
      messages.value.push(data)
      nextTick().then(scrollToBottom)
    } else if (userStore.userInfo?.role === USER_ROLE.ADMIN && currentChat.value?.type === 'appeal') {
      // 管理员在申诉聊天中，如果收到来自该用户的消息，也要显示
      if (data.senderId === currentChat.value.id || data.receiverId === currentChat.value.id) {
        messages.value.push(data)
        nextTick().then(scrollToBottom)
      }
    } else {
      console.log('消息不属于当前聊天，忽略')
    }
  }
  
  window.addEventListener('new-chat-message', handleNewMessage)
  
  // 点击其他地方关闭右键菜单
  document.addEventListener('click', closeMessageContextMenu)
})

onUnmounted(() => {
  // 清理定时器
  stopMemberStatusRefresh()
  // 移除事件监听
  window.removeEventListener('new-chat-message', handleNewMessage)
  document.removeEventListener('click', closeMessageContextMenu)
})
</script>

<style scoped>
.chat-container {
  height: 100%;
  padding: 20px;
}

.friend-list-card, .chat-card {
  height: 100%;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 5px;
}

.chat-item:hover, .chat-item.active {
  background-color: #f5f7fa;
}

.chat-item-info {
  margin-left: 10px;
  flex: 1;
}

.chat-item-name {
  font-weight: 500;
}

.chat-item-status,
.chat-item-desc {
  font-size: 12px;
  color: #909399;
}

.status-1 {
  color: #67c23a;
}

.status-2 {
  color: #e6a23c;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 16px;
}

.chat-title-section {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.chat-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.ws-status-tag {
  font-size: 12px;
  padding: 4px 10px;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
}

.my-message {
  flex-direction: row-reverse;
}

.other-message {
  flex-direction: row;
}

.message-content {
  margin: 0 10px;
  max-width: 60%;
}

.sender-name {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
  font-weight: 500;
}

.message-text {
  background-color: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  word-wrap: break-word;
  /* 允许选择和复制消息内容 */
  -webkit-user-select: text;
  -moz-user-select: text;
  -ms-user-select: text;
  user-select: text;
}

.message-text.recalled {
  color: #909399;
  font-style: italic;
}

.my-message .message-text {
  background-color: #409eff;
  color: #fff;
  /* 允许选择和复制消息内容 */
  -webkit-user-select: text;
  -moz-user-select: text;
  -ms-user-select: text;
  user-select: text;
}

.my-message .message-text.recalled {
  background-color: #dcdfe6;
  color: #909399;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.message-input {
  display: flex;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}

.group-stats {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-stats {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.member-item:hover {
  background-color: #f5f7fa;
}

.member-info {
  margin-left: 12px;
  flex: 1;
}

.member-name {
  font-weight: 500;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
}

.member-status {
  font-size: 12px;
  color: #909399;
}

.member-status.status-1 {
  color: #67c23a;
  font-weight: 500;
}

.member-status.status-2 {
  color: #e6a23c;
  font-weight: 500;
}

/* 消息右键菜单样式 */
.message-context-menu {
  position: fixed;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  min-width: 160px;
  z-index: 9999;
  animation: fadeIn 0.1s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.context-menu-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  color: #303133;
  font-size: 14px;
}

.context-menu-item:hover {
  background-color: #f5f7fa;
}

.context-menu-item .el-icon {
  margin-right: 10px;
  font-size: 16px;
}

.context-menu-item.danger {
  color: #f56c6c;
}

.context-menu-item.danger:hover {
  background-color: #fef0f0;
}

.context-menu-item.warning {
  color: #e6a23c;
}

.context-menu-item.warning:hover {
  background-color: #fdf6ec;
}

.context-menu-divider {
  height: 1px;
  background-color: #e4e7ed;
  margin: 6px 0;
}

/* 引用消息提示条 */
.quoted-message-bar {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-top: 1px solid #e4e7ed;
}

/* 转发目标项 */
.forward-target-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 6px;
  transition: background-color 0.2s;
  margin-bottom: 5px;
}

.forward-target-item:hover {
  background-color: #f5f7fa;
}

.forward-target-name {
  margin-left: 10px;
  flex: 1;
  font-size: 14px;
}

.forward-target-item .el-icon {
  margin-left: 10px;
  font-size: 18px;
}

/* 处罚通知样式 */
.penalty-notification {
  padding: 10px 0;
}

.penalty-content {
  margin-top: 20px;
  padding: 15px;
  background-color: #fef0f0;
  border-radius: 8px;
  border-left: 4px solid #f56c6c;
}

.penalty-content p {
  margin: 0 0 15px 0;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}

.penalty-reason,
.penalty-time {
  margin-top: 10px;
  font-size: 13px;
  color: #606266;
}

.penalty-reason strong,
.penalty-time strong {
  color: #303133;
}

.appeal-hint {
  margin-top: 20px;
  padding: 12px;
  background-color: #ecf5ff;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #409eff;
}

.appeal-hint .el-icon {
  font-size: 16px;
}

/* 禁言提示样式 */
.mute-hint {
  padding: 10px 20px;
  background-color: #fdf6ec;
  border-bottom: 1px solid #e4e7ed;
}

/* 申诉状态横幅 */
.appeal-status-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  margin-bottom: 10px;
  background-color: #fef0f0;
  border-radius: 4px;
  color: #f56c6c;
  font-size: 13px;
  border-left: 4px solid #f56c6c;
}
</style>