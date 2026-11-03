<template>
  <div class="friends-container">
    <el-card>
      <div class="header">
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>添加好友
        </el-button>
      </div>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我的好友" name="friends">
          <el-scrollbar style="height: calc(100vh - 250px)">
            <div v-if="friendList.length" class="friend-list">
              <div 
                v-for="friend in friendList" 
                :key="friend.id" 
                class="friend-item"
                @contextmenu.prevent="handleContextMenu($event, friend)"
              >
                <el-avatar :size="40" :src="getAvatarUrl(friend.avatar)">
                  {{ friend.nickname?.[0] }}
                </el-avatar>
                <div class="friend-info">
                  <div class="friend-name">
                    <span>{{ friend.nickname }}</span>
                    <el-tag v-if="friend.remark" type="info" size="small" style="margin-left: 10px">{{ friend.remark }}</el-tag>
                    <el-tag v-if="friend.isBlocked" type="danger" size="small" style="margin-left: 10px">已拉黑</el-tag>
                  </div>
                  <div class="friend-status">
                    <span class="status" :class="'status-' + friend.status">
                      {{ friend.status === 1 ? '在线' : friend.status === 2 ? '忙碌' : '离线' }}
                    </span>
                  </div>
                </div>
                <el-dropdown class="friend-actions">
                  <el-button type="link"><el-icon><More /></el-icon></el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="handleChat(friend)" :disabled="friend.isBlocked">发起聊天</el-dropdown-item>
                      <el-dropdown-item @click="showRemarkDialog(friend)">修改备注</el-dropdown-item>
                      <el-dropdown-item @click="handleBlockFriend(friend)" divided>
                        {{ friend.isBlocked ? '取消拉黑' : '拉黑好友' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="handleRemove(friend)" divided>删除好友</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <el-empty v-else description="暂无好友" />
          </el-scrollbar>
        </el-tab-pane>
        
        <el-tab-pane :label="`好友申请(${pendingList.length})`" name="friendPending">
          <el-scrollbar style="height: calc(100vh - 250px)">
            <div v-if="pendingList.length" class="pending-list">
              <div v-for="request in pendingList" :key="request.id" class="pending-item">
                <div class="pending-item-header">
                  <el-avatar :size="40" :src="getAvatarUrl(request.applicantAvatar)">
                    {{ request.applicantName?.[0] || '用户' }}
                  </el-avatar>
                  <div class="pending-item-info">
                    <div class="pending-item-name">
                      <span>{{ request.applicantName }}</span>
                      <el-tag type="info" size="small" style="margin-left: 10px">{{ request.applicantAccount }}</el-tag>
                    </div>
                    <div class="pending-item-time">申请时间: {{ formatTime(request.applyTime) }}</div>
                  </div>
                </div>
                <div class="pending-item-actions">
                  <el-button type="success" size="small" @click="handleAccept(request.id)">接受</el-button>
                  <el-button type="danger" size="small" @click="handleReject(request.id)">拒绝</el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无好友申请" />
          </el-scrollbar>
        </el-tab-pane>
        
        <el-tab-pane :label="`群申请(${groupPendingApplies.length})`" name="groupPending">
          <el-scrollbar style="height: calc(100vh - 250px)">
            <div v-if="groupPendingApplies.length" class="pending-list">
              <div v-for="apply in groupPendingApplies" :key="apply.id" class="pending-item">
                <div class="pending-item-header">
                  <el-avatar :size="40" :src="getAvatarUrl(apply.applicantAvatar)">
                    {{ apply.applicantName?.[0] || '用户' }}
                  </el-avatar>
                  <div class="pending-item-info">
                    <div class="pending-item-name">
                      <span>{{ apply.applicantName }}</span>
                      <el-tag type="info" size="small" style="margin-left: 10px">{{ apply.applicantAccount }}</el-tag>
                      <el-tag type="warning" size="small" style="margin-left: 5px">{{ apply.groupName }}</el-tag>
                    </div>
                    <div class="pending-item-time">申请时间: {{ formatTime(apply.applyTime) }}</div>
                  </div>
                </div>
                <div class="pending-item-actions">
                  <el-button type="success" size="small" @click="handleAcceptGroupApply(apply.id)">接受</el-button>
                  <el-button type="danger" size="small" @click="handleRejectGroupApply(apply.id)">拒绝</el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无群申请" />
          </el-scrollbar>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 添加好友对话框 -->
    <el-dialog v-model="showAddDialog" title="添加好友" width="400px">
      <el-input v-model="searchKeyword" placeholder="输入用户名/账号/昵称搜索" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch" style="margin-top: 10px; width: 100%">搜索</el-button>
      
      <el-scrollbar v-if="searchResults.length" style="max-height: 300px; margin-top: 15px">
        <div v-for="user in searchResults" :key="user.id" class="search-item">
          <el-avatar :size="36" :src="getAvatarUrl(user.avatar)">
            {{ user.nickname?.[0] }}
          </el-avatar>
          <span class="search-info">{{ user.nickname }} ({{ user.account }})</span>
          <el-button type="primary" size="small" @click="handleAddFriend(user.id)">添加</el-button>
        </div>
      </el-scrollbar>
    </el-dialog>

    <!-- 修改备注对话框 -->
    <el-dialog v-model="showRemarkDialogVisible" title="修改备注" width="400px">
      <el-form :model="remarkForm" label-width="80px">
        <el-form-item label="备注名">
          <el-input 
            v-model="remarkForm.remark" 
            placeholder="请输入备注名，留空则使用昵称"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRemarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateRemark">确定</el-button>
      </template>
    </el-dialog>

    <!-- 举报对话框 -->
    <el-dialog v-model="showReportDialogVisible" title="举报好友" width="500px">
      <el-form :model="reportForm" label-width="100px">
        <el-form-item label="举报对象">
          <span>{{ currentFriend?.nickname }}</span>
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

    <!-- 右键菜单 -->
    <div 
      v-show="contextMenuVisible" 
      class="context-menu"
      :style="{
        left: contextMenuPosition.x + 'px',
        top: contextMenuPosition.y + 'px'
      }"
    >
      <div class="context-menu-item" @click="showRemarkDialog(contextMenuFriend)">
        <el-icon><Edit /></el-icon>
        <span>修改备注</span>
      </div>
      <div class="context-menu-item" @click="handleBlockFriend(contextMenuFriend); closeContextMenu()">
        <el-icon><CircleClose /></el-icon>
        <span>{{ contextMenuFriend?.isBlocked ? '取消拉黑' : '拉黑好友' }}</span>
      </div>
      <div class="context-menu-divider"></div>
      <div class="context-menu-item danger" @click="handleRemove(contextMenuFriend); closeContextMenu()">
        <el-icon><Delete /></el-icon>
        <span>删除好友</span>
      </div>
      <div class="context-menu-item warning" @click="showReportDialog(contextMenuFriend)">
        <el-icon><Warning /></el-icon>
        <span>举报</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, More, Edit, CircleClose, Delete, Warning } from '@element-plus/icons-vue'
import { getFriendList, getPendingRequests, searchUser, addFriend, acceptFriend, rejectFriend, removeFriend, updateRemark, blockFriend, unblockFriend } from '../api/friend'
import { getUserGroups, getPendingApplies, acceptApply, rejectApply } from '../api/group'
import { deletePrivateMessages } from '../api/message'
import { submitReport } from '../api/admin'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('friends')
const friendList = ref([])
const pendingList = ref([])
const groupPendingApplies = ref([])
const showAddDialog = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])

// 备注相关
const showRemarkDialogVisible = ref(false)
const currentFriend = ref(null)
const remarkForm = ref({
  remark: ''
})

// 右键菜单相关
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const contextMenuFriend = ref(null)

// 举报对话框
const showReportDialogVisible = ref(false)
const reportForm = ref({
  reasonCode: null,
  reason: '',
  includeMessages: 0
})

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

const loadPendingRequests = async () => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    console.warn('用户信息未加载，跳过加载待处理申请')
    return
  }
  
  try {
    console.log('正在加载好友申请列表, userId:', userStore.userInfo.id)
    const res = await getPendingRequests(userStore.userInfo.id)
    console.log('待处理申请数据:', res.data)
    console.log('待处理申请数量:', res.data?.length || 0)
    pendingList.value = res.data || []
  } catch (error) {
    console.error('加载待处理申请失败:', error)
  }
}

const loadGroupPendingApplies = async () => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    return
  }
  
  try {
    // 获取用户加入的所有群组
    const groupsRes = await getUserGroups(userStore.userInfo.id)
    const groups = groupsRes.data || []
    
    // 获取每个群组的待审核申请
    const allApplies = []
    for (const group of groups) {
      try {
        const appliesRes = await getPendingApplies(group.groupId)
        const applies = appliesRes.data || []
        allApplies.push(...applies)
      } catch (error) {
        console.error(`加载群组${group.groupId}的申请失败:`, error)
      }
    }
    
    groupPendingApplies.value = allApplies
  } catch (error) {
    console.error('加载群申请失败:', error)
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value) {
    searchResults.value = []
    return
  }
  
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  try {
    const res = await searchUser(searchKeyword.value)
    // 过滤掉已经是好友的用户和自己
    const friendIds = friendList.value.map(f => f.id)
    searchResults.value = res.data.filter(user => 
      user.id !== userStore.userInfo.id && !friendIds.includes(user.id)
    )
    
    if (searchResults.value.length === 0) {
      ElMessage.info('未找到可添加的用户')
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
    ElMessage.error('搜索失败')
  }
}

const handleAddFriend = async (friendId) => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  try {
    await addFriend({ userId: userStore.userInfo.id, friendId })
    ElMessage.success('好友申请已发送')
    showAddDialog.value = false
    searchResults.value = []
    searchKeyword.value = ''
    loadFriends()
  } catch (error) {
    console.error('添加好友失败:', error)
    // 错误信息已经在 request.js 中显示了，这里不需要重复显示
  }
}

const handleAccept = async (relationId) => {
  try {
    await acceptFriend(relationId)
    ElMessage.success('已接受好友申请')
    loadFriends()
    loadPendingRequests()
  } catch (error) {
    console.error('接受好友申请失败:', error)
  }
}

const handleReject = async (relationId) => {
  try {
    await ElMessageBox.confirm('确定拒绝该好友申请吗?', '提示')
    await rejectFriend(relationId)
    ElMessage.success('已拒绝')
    loadPendingRequests()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝好友申请失败:', error)
    }
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 获取头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  const config = userStore.serverConfig
  const baseUrl = config?.baseUrl || 'http://localhost:8080'
  return avatar.startsWith('http') ? avatar : `${baseUrl}/${avatar}`
}

const handleTabChange = (tabName) => {
  if (tabName === 'friendPending') {
    loadPendingRequests()
  } else if (tabName === 'friends') {
    loadFriends()
  } else if (tabName === 'groupPending') {
    loadGroupPendingApplies()
  }
}

const handleRemove = async (friend) => {
  try {
    await ElMessageBox.confirm(`确定删除好友 ${friend.nickname} 吗？\n\n注意：删除好友将同时清空聊天记录，此操作不可恢复！`, '提示', {
      type: 'warning'
    })
    
    // 删除好友（后端会自动清空聊天记录）
    await removeFriend(userStore.userInfo.id, friend.id)
    ElMessage.success('已删除好友')
    loadFriends()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除好友失败:', error)
    }
  }
}

const handleChat = (friend) => {
  router.push('/chat')
}

// 显示修改备注对话框
const showRemarkDialog = (friend) => {
  currentFriend.value = friend
  remarkForm.value.remark = friend.remark || ''
  showRemarkDialogVisible.value = true
}

// 更新备注
const handleUpdateRemark = async () => {
  if (!currentFriend.value) return
  
  try {
    await updateRemark(userStore.userInfo.id, currentFriend.value.id, remarkForm.value.remark)
    ElMessage.success('备注修改成功')
    showRemarkDialogVisible.value = false
    await loadFriends()
  } catch (error) {
    console.error('修改备注失败:', error)
  }
}

// 拉黑/取消拉黑好友
const handleBlockFriend = async (friend) => {
  const action = friend.isBlocked ? '取消拉黑' : '拉黑'
  try {
    await ElMessageBox.confirm(
      `确定要${action}好友 ${friend.nickname} 吗？`,
      '提示'
    )
    
    if (friend.isBlocked) {
      await unblockFriend(userStore.userInfo.id, friend.id)
      ElMessage.success('已取消拉黑')
    } else {
      await blockFriend(userStore.userInfo.id, friend.id)
      ElMessage.success('已拉黑该好友')
    }
    
    await loadFriends()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}好友失败:`, error)
    }
  }
}

// 接受群申请
const handleAcceptGroupApply = async (applyId) => {
  try {
    await acceptApply(applyId, userStore.userInfo.id)
    ElMessage.success('已通过申请')
    await loadGroupPendingApplies()
  } catch (error) {
    console.error('接受群申请失败:', error)
  }
}

// 拒绝群申请
const handleRejectGroupApply = async (applyId) => {
  try {
    await ElMessageBox.confirm('确定拒绝该入群申请吗?', '提示')
    await rejectApply(applyId, userStore.userInfo.id)
    ElMessage.success('已拒绝')
    await loadGroupPendingApplies()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝群申请失败:', error)
    }
  }
}

// 右键菜单处理
const handleContextMenu = (event, friend) => {
  event.preventDefault()
  contextMenuFriend.value = friend
  contextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
  contextMenuVisible.value = true
}

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuFriend.value = null
}

// 显示举报对话框
const showReportDialog = (friend) => {
  // 检查是否举报自己
  if (friend.id === userStore.userInfo.id) {
    ElMessage.warning('不能举报自己')
    return
  }
  
  currentFriend.value = friend
  reportForm.value = {
    reasonCode: null,
    reason: '',
    includeMessages: 0
  }
  showReportDialogVisible.value = true
  closeContextMenu()
}

// 提交举报
const handleSubmitReport = async () => {
  if (!currentFriend.value) return
  if (!reportForm.value.reasonCode) {
    ElMessage.warning('请选择举报原因')
    return
  }
  
  try {
    await submitReport({
      reporterId: userStore.userInfo.id,
      reportedUserId: currentFriend.value.id,
      reportType: 1, // 用户举报
      reasonCode: reportForm.value.reasonCode,
      reason: reportForm.value.reason,
      includeMessages: reportForm.value.includeMessages
    })
    ElMessage.success('举报已提交，管理员将尽快处理')
    showReportDialogVisible.value = false
  } catch (error) {
    console.error('提交举报失败:', error)
  }
}

onMounted(() => {
  loadFriends()
  loadPendingRequests()
  loadGroupPendingApplies()
  // 点击页面其他地方关闭右键菜单
  document.addEventListener('click', closeContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeContextMenu)
})
</script>

<style scoped>
.friends-container {
  padding: 20px;
  height: 100%;
}

.header {
  margin-bottom: 20px;
}

.status {
  font-size: 12px;
}

.status-1 {
  color: #67c23a;
}

.status-2 {
  color: #e6a23c;
}

.friend-list {
  padding: 10px;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.friend-item:hover {
  background: #e8eaed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.friend-info {
  flex: 1;
  margin-left: 12px;
}

.friend-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #303133;
}

.friend-status {
  font-size: 12px;
}

.friend-actions {
  margin-left: auto;
}

.search-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
}

.search-info {
  flex: 1;
  margin-left: 10px;
}

.pending-list {
  padding: 10px;
}

.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.pending-item:hover {
  background: #e8eaed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pending-item-header {
  display: flex;
  align-items: center;
  flex: 1;
}

.pending-item-info {
  margin-left: 15px;
}

.pending-item-name {
  margin-bottom: 5px;
}

.pending-item-time {
  font-size: 12px;
  color: #909399;
}

.pending-item-actions {
  display: flex;
  gap: 10px;
}

/* 右键菜单样式 */
.context-menu {
  position: fixed;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  padding: 6px 0;
  min-width: 160px;
  z-index: 9999;
  animation: fadeIn 0.15s ease-in-out;
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
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: #303133;
}

.context-menu-item:hover {
  background-color: #f5f7fa;
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
  background-color: #ebeef5;
  margin: 4px 0;
}

.context-menu-item .el-icon {
  font-size: 16px;
}
</style>
