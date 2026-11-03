<template>
  <div class="admin-container">
    <el-card>
      <div class="header">
        <h2>管理员控制台</h2>
        <el-tag type="danger" size="large">管理员: {{ userStore.userInfo?.nickname }}</el-tag>
      </div>
      
      <el-tabs v-model="activeTab">
        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="users">
          <el-table :data="userList" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="头像" width="80">
              <template #default="{ row }">
                <el-avatar :size="40" :src="getAvatarUrl(row.avatar)">
                  {{ row.nickname?.[0] }}
                </el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="nickname" label="昵称" width="120" />
            <el-table-column prop="account" label="账号" width="120" />
            <el-table-column label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="row.role === USER_ROLE.ADMIN ? 'danger' : 'info'">
                  {{ row.role === USER_ROLE.ADMIN ? '管理员' : '普通用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isBanned === BAN_STATUS.BANNED" type="danger">已封号</el-tag>
                <el-tag v-else-if="row.isMuted === MUTE_STATUS.MUTED" type="warning">已禁言</el-tag>
                <el-tag v-else type="success">正常</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300">
              <template #default="{ row }">
                <!-- 封号/解封按钮 - 不能对自己操作 -->
                <el-button 
                  v-if="row.id !== userStore.userInfo?.id && row.isBanned !== BAN_STATUS.BANNED"
                  type="danger" 
                  size="small" 
                  @click="showBanDialog(row)"
                >
                  封号
                </el-button>
                <el-button 
                  v-if="row.id !== userStore.userInfo?.id && row.isBanned === BAN_STATUS.BANNED"
                  type="success" 
                  size="small" 
                  @click="handleUnban(row)"
                >
                  解封
                </el-button>
                
                <!-- 禁言/解禁按钮 - 不能对自己操作 -->
                <el-button 
                  v-if="row.id !== userStore.userInfo?.id && row.isMuted !== MUTE_STATUS.MUTED"
                  type="warning" 
                  size="small" 
                  @click="showMuteDialog(row)"
                >
                  禁言
                </el-button>
                <el-button 
                  v-if="row.id !== userStore.userInfo?.id && row.isMuted === MUTE_STATUS.MUTED"
                  type="success" 
                  size="small" 
                  @click="handleUnmute(row)"
                >
                  解禁
                </el-button>
                
                <!-- 设为管理员按钮 - 不能对自己操作 -->
                <el-button 
                  v-if="row.id !== userStore.userInfo?.id && row.role !== USER_ROLE.ADMIN"
                  type="primary" 
                  size="small" 
                  @click="handleSetRole(row, USER_ROLE.ADMIN)"
                >
                  设为管理员
                </el-button>
                
                <!-- 自己显示提示 -->
                <el-tag v-if="row.id === userStore.userInfo?.id" type="info" size="small">
                  当前用户
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <!-- 举报管理 -->
        <el-tab-pane :label="`举报管理(${pendingReports.length})`" name="reports">
          <el-table :data="pendingReports" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="举报类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.reportType === REPORT_TYPE.USER ? 'danger' : 'warning'">
                  {{ row.reportType === REPORT_TYPE.USER ? '用户举报' : '消息举报' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="被举报对象" width="120">
              <template #default="{ row }">
                <span v-if="row.reportType === REPORT_TYPE.USER">用户 {{ row.reportedUserId }}</span>
                <span v-else>消息 {{ row.messageId }}</span>
              </template>
            </el-table-column>
            <el-table-column label="举报原因" min-width="150">
              <template #default="{ row }">
                <el-tag :type="getReasonTagType(row.reasonCode)" size="small">
                  {{ getReasonText(row.reasonCode) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="详细说明" min-width="150" show-overflow-tooltip />
            <el-table-column label="聊天记录" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.includeMessages === 1" type="success" size="small">包含</el-tag>
                <el-tag v-else type="info" size="small">不包含</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="举报时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="handleProcessReport(row, REPORT_STATUS.APPROVED)">
                  通过
                </el-button>
                <el-button type="danger" size="small" @click="handleProcessReport(row, REPORT_STATUS.REJECTED)">
                  驳回
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty v-if="pendingReports.length === 0" description="暂无待处理的举报" />
        </el-tab-pane>
        
        <!-- 申诉管理 -->
        <el-tab-pane :label="`申诉管理(${pendingAppeals.length})`" name="appeals">
          <el-table :data="pendingAppeals" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="头像" width="80">
              <template #default="{ row }">
                <el-avatar :size="40" :src="getAvatarUrl(row.avatar)">
                  {{ row.nickname?.[0] }}
                </el-avatar>
              </template>
            </el-table-column>
            <el-table-column label="申诉人" width="150">
              <template #default="{ row }">
                <div>{{ row.nickname }}</div>
                <div style="font-size: 12px; color: #909399">{{ row.account }}</div>
              </template>
            </el-table-column>
            <el-table-column label="申诉内容" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.content?.replace('[申诉] ', '') }}
              </template>
            </el-table-column>
            <el-table-column label="申诉时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.sendTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleChatWithUser(row)">
                  聊天处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty v-if="pendingAppeals.length === 0" description="暂无待处理的申诉" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 封号对话框 -->
    <el-dialog v-model="showBanDialogVisible" title="封号用户" width="500px">
      <el-form :model="banForm" label-width="100px">
        <el-form-item label="用户">
          <el-input :value="currentUser?.nickname" disabled />
        </el-form-item>
        <el-form-item label="封号原因" required>
          <el-input 
            v-model="banForm.reason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入封号原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBanDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleBan">确定封号</el-button>
      </template>
    </el-dialog>
    
    <!-- 禁言对话框 -->
    <el-dialog v-model="showMuteDialogVisible" title="禁言用户" width="500px">
      <el-form :model="muteForm" label-width="100px">
        <el-form-item label="用户">
          <el-input :value="currentUser?.nickname" disabled />
        </el-form-item>
        <el-form-item label="禁言原因" required>
          <el-input 
            v-model="muteForm.reason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入禁言原因"
          />
        </el-form-item>
        <el-form-item label="禁言时长">
          <el-input-number 
            v-model="muteForm.durationMinutes" 
            :min="0" 
            :max="10080"
            placeholder="0表示永久禁言"
          />
          <span style="margin-left: 10px; color: #909399">分钟（0表示永久）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMuteDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handleMute">确定禁言</el-button>
      </template>
    </el-dialog>
    
    <!-- 处罚选择对话框 -->
    <el-dialog v-model="showPenaltyDialogVisible" title="选择处罚措施" width="500px">
      <el-form :model="penaltyForm" label-width="100px">
        <el-form-item label="被举报对象">
          <el-input :value="currentReport?.reportedUserId ? `用户 ${currentReport.reportedUserId}` : `消息 ${currentReport?.messageId}`" disabled />
        </el-form-item>
        <el-form-item label="举报原因">
          <el-tag :type="getReasonTagType(currentReport?.reasonCode)" size="small">
            {{ getReasonText(currentReport?.reasonCode) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="处罚类型" required>
          <el-select v-model="penaltyForm.penaltyType" placeholder="请选择处罚类型" style="width: 100%">
            <el-option
              v-for="option in penaltyOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPenaltyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPenaltyAndHandleReport">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllUsers, banUser, unbanUser, muteUser, unmuteUser, setUserRole, getPendingReports, handleReport, handleReportWithPenalty, getPendingAppeals } from '../api/admin'
import { useUserStore } from '../store/user'
import { useRouter } from 'vue-router'
import { 
  USER_ROLE, 
  BAN_STATUS, 
  MUTE_STATUS,
  REPORT_TYPE,
  REPORT_STATUS,
  REPORT_REASON_TEXT
} from '../constants'

const userStore = useUserStore()
const router = useRouter()
const activeTab = ref('users')
const userList = ref([])
const pendingReports = ref([])
const pendingAppeals = ref([])
const currentUser = ref(null)

// 封号对话框
const showBanDialogVisible = ref(false)
const banForm = ref({
  reason: ''
})

// 禁言对话框
const showMuteDialogVisible = ref(false)
const muteForm = ref({
  reason: '',
  durationMinutes: 0
})

// 处罚选择对话框
const showPenaltyDialogVisible = ref(false)
const currentReport = ref(null)
const penaltyForm = ref({
  penaltyType: 0
})

// 加载用户列表
const loadUsers = async () => {
  try {
    const res = await getAllUsers()
    userList.value = res.data || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  }
}

// 加载举报列表
const loadReports = async () => {
  try {
    const res = await getPendingReports()
    pendingReports.value = res.data || []
  } catch (error) {
    console.error('加载举报列表失败:', error)
  }
}

// 加载申诉列表
const loadAppeals = async () => {
  try {
    const res = await getPendingAppeals()
    pendingAppeals.value = res.data || []
  } catch (error) {
    console.error('加载申诉列表失败:', error)
  }
}

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

// 获取举报原因文本（使用常量）
const getReasonText = (reasonCode) => {
  return REPORT_REASON_TEXT[reasonCode] || '未知'
}

// 获取举报原因标签类型
const getReasonTagType = (reasonCode) => {
  const types = {
    [REPORT_REASON.SPAM]: 'warning',
    [REPORT_REASON.HARASSMENT]: 'danger',
    [REPORT_REASON.FRAUD]: 'danger',
    [REPORT_REASON.INAPPROPRIATE]: 'warning',
    [REPORT_REASON.IMPERSONATION]: 'danger',
    [REPORT_REASON.PRIVACY]: 'danger',
    [REPORT_REASON.OTHER]: 'info'
  }
  return types[reasonCode] || 'info'
}

// 处罚类型选项
const penaltyOptions = [
  { value: 0, label: '无处罚' },
  { value: 1, label: '警告' },
  { value: 2, label: '禁言' },
  { value: 3, label: '封号' },
  { value: 4, label: '永久封号' }
]

// 显示封号对话框
const showBanDialog = (user) => {
  currentUser.value = user
  banForm.value.reason = ''
  showBanDialogVisible.value = true
}

// 封号
const handleBan = async () => {
  if (!banForm.value.reason.trim()) {
    ElMessage.warning('请输入封号原因')
    return
  }
  
  try {
    await banUser(currentUser.value.id, userStore.userInfo.id, banForm.value.reason)
    ElMessage.success('封号成功')
    showBanDialogVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '封号失败')
  }
}

// 解封
const handleUnban = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要解封用户 ${user.nickname} 吗？`, '提示')
    await unbanUser(user.id, userStore.userInfo.id)
    ElMessage.success('解封成功')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '解封失败')
    }
  }
}

// 显示禁言对话框
const showMuteDialog = (user) => {
  currentUser.value = user
  muteForm.value.reason = ''
  muteForm.value.durationMinutes = 0
  showMuteDialogVisible.value = true
}

// 禁言
const handleMute = async () => {
  if (!muteForm.value.reason.trim()) {
    ElMessage.warning('请输入禁言原因')
    return
  }
  
  try {
    await muteUser(
      currentUser.value.id, 
      userStore.userInfo.id, 
      muteForm.value.reason,
      muteForm.value.durationMinutes > 0 ? muteForm.value.durationMinutes : null
    )
    ElMessage.success('禁言成功')
    showMuteDialogVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '禁言失败')
  }
}

// 解禁
const handleUnmute = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要解除用户 ${user.nickname} 的禁言吗？`, '提示')
    await unmuteUser(user.id, userStore.userInfo.id)
    ElMessage.success('解禁成功')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '解禁失败')
    }
  }
}

// 设置角色
const handleSetRole = async (user, role) => {
  try {
    await ElMessageBox.confirm(
      `确定要将用户 ${user.nickname} 设为${role === USER_ROLE.ADMIN ? '管理员' : '普通用户'}吗？`,
      '提示'
    )
    await setUserRole(user.id, userStore.userInfo.id, role)
    ElMessage.success('设置成功')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '设置失败')
    }
  }
}

// 处理举报
const handleProcessReport = async (report, status) => {
  if (status === REPORT_STATUS.APPROVED) {
    // 如果是通过举报，显示处罚选择对话框
    currentReport.value = report
    penaltyForm.value.penaltyType = 0 // 默认无处罚
    showPenaltyDialogVisible.value = true
  } else {
    // 如果是驳回举报，直接处理
    const statusText = '驳回'
    try {
      await ElMessageBox.confirm(`确定要${statusText}该举报吗？`, '提示')
      await handleReport(report.id, userStore.userInfo.id, status, `${statusText}举报`)
      ElMessage.success(`${statusText}成功`)
      await loadReports()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || `${statusText}失败`)
      }
    }
  }
}

// 确认处罚并处理举报
const confirmPenaltyAndHandleReport = async () => {
  if (!currentReport.value) return
  
  const statusText = '通过'
  try {
    await handleReportWithPenalty(
      currentReport.value.id, 
      userStore.userInfo.id, 
      REPORT_STATUS.APPROVED, // 状态为通过
      `${statusText}举报`, 
      penaltyForm.value.penaltyType
    )
    ElMessage.success(`${statusText}成功`)
    showPenaltyDialogVisible.value = false
    await loadReports()
  } catch (error) {
    ElMessage.error(error.message || `${statusText}失败`)
  }
}

// 处理申诉 - 跳转到与用户聊天页面
const handleChatWithUser = async (appeal) => {
  try {
    // 跳转到聊天页面，并预选该用户
    router.push(`/chat`) // 先跳转到聊天页
    // 注意：这里需要通知父组件或全局状态，以便聊天组件能自动选中该用户
    // 这里暂时只是跳转，实际选中用户需要进一步实现
    console.log('准备与用户聊天:', appeal.nickname, appeal.userId)
  } catch (error) {
    console.error('跳转到聊天页面失败:', error)
    ElMessage.error('跳转失败')
  }
}

onMounted(() => {
  loadUsers()
  loadReports()
  loadAppeals()
})
</script>

<style scoped>
.admin-container {
  padding: 20px;
  height: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #409eff;
}

.header h2 {
  margin: 0;
  color: #303133;
}
</style>
