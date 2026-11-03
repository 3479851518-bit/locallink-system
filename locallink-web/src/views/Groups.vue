<template>
  <div class="groups-container">
    <el-card>
      <div class="header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索群组名称"
          style="width: 300px; margin-right: 10px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>创建群组
        </el-button>
      </div>
      
      <el-scrollbar style="height: calc(100vh - 220px); margin-top: 20px">
        <!-- 搜索结果显示 -->
        <div v-if="searchResults.length > 0" class="search-results">
          <div class="section-title">搜索结果</div>
          <el-card 
            v-for="group in searchResults" 
            :key="group.id" 
            class="group-item"
            shadow="hover"
          >
            <div class="group-item-content">
              <div class="group-info">
                <el-avatar shape="square" :size="50" :src="getGroupAvatarUrl(group.groupAvatar)">
                  {{ group.groupName?.[0] }}
                </el-avatar>
                <div class="group-detail">
                  <div class="group-name">{{ group.groupName }}</div>
                  <div class="group-id">群ID: {{ group.groupId }}</div>
                </div>
              </div>
              <el-button type="success" size="small" @click="handleJoinGroup(group)">加入群组</el-button>
            </div>
          </el-card>
        </div>
        
        <!-- 我的群组列表 -->
        <div class="my-groups">
          <div class="section-title">我的群组</div>
          <template v-if="groupList.length">
            <el-card 
              v-for="group in groupList" 
              :key="group.id" 
              class="group-item"
              shadow="hover"
            >
              <div class="group-item-content">
                <div class="group-info">
                  <el-avatar shape="square" :size="50" :src="getGroupAvatarUrl(group.groupAvatar)">
                    {{ group.groupName?.[0] }}
                  </el-avatar>
                  <div class="group-detail">
                    <div class="group-name">{{ group.groupName }}</div>
                    <div class="group-time">创建时间: {{ formatTime(group.createTime) }}</div>
                  </div>
                </div>
                <el-button type="primary" size="small" @click="enterGroup(group)">进入群聊</el-button>
                <el-button type="warning" size="small" @click="showManageDialog(group)">管理群组</el-button>
              </div>
            </el-card>
          </template>
          <el-empty v-if="groupList.length === 0 && searchResults.length === 0" description="暂无群组" />
        </div>
      </el-scrollbar>
    </el-card>

    <!-- 创建群组对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建群组" width="400px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="群组名称">
          <el-input v-model="createForm.groupName" placeholder="请输入群组名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 群管理对话框 -->
    <el-dialog v-model="showManageDialogVisible" :title="`管理群组 - ${currentGroup?.groupName || ''}`" width="800px">
      <el-tabs v-model="manageTab">
        <!-- 群成员管理 -->
        <el-tab-pane label="成员管理" name="members">
          <div class="member-list">
            <div v-for="member in memberList" :key="member.userId" class="member-item">
              <el-avatar :size="40" :src="getAvatarUrl(member.avatar)">
                {{ getAvatarText(member) }}
              </el-avatar>
              <div class="member-info">
                <div class="member-name">
                  {{ member.nickname }} ({{ member.account }})
                  <el-tag :type="getRoleTagType(member.role)" size="small" style="margin-left: 10px">
                    {{ member.roleName }}
                  </el-tag>
                </div>
                <div class="member-time">加入时间: {{ formatTime(member.joinTime) }}</div>
              </div>
              <div class="member-actions" v-if="canManageMember(member.userId)">
                <el-dropdown trigger="click">
                  <el-button type="link" size="small">操作</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item 
                        v-if="currentGroup?.creatorId === userStore.userInfo?.id && member.role === 0"
                        @click="handleSetAdmin(member.userId, true)"
                      >
                        设为管理员
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="currentGroup?.creatorId === userStore.userInfo?.id && member.role === 1"
                        @click="handleSetAdmin(member.userId, false)"
                      >
                        取消管理员
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="currentGroup?.creatorId === userStore.userInfo?.id && member.role !== 2"
                        divided
                        @click="handleTransferOwner(member.userId)"
                      >
                        转让群主
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="(currentGroup?.creatorId === userStore.userInfo?.id || isCurrentUserAdmin()) && member.role !== 2"
                        divided
                        @click="handleRemoveMember(member.userId)"
                      >
                        移除成员
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 群设置 -->
        <el-tab-pane label="群设置" name="settings">
          <el-form :model="groupSettings" label-width="100px" style="margin-top: 20px">
            <el-form-item label="群头像" v-if="canEditGroupInfo()">
              <el-upload
                class="group-avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeGroupAvatarUpload"
                :http-request="handleUploadGroupAvatar"
                accept="image/*"
              >
                <el-avatar 
                  shape="square" 
                  :size="80" 
                  :src="groupAvatarPreview || getGroupAvatarUrl(currentGroup?.groupAvatar) || ''"
                >
                  {{ currentGroup?.groupName?.[0] || '群' }}
                </el-avatar>
                <div class="avatar-overlay">
                  <el-icon :size="20"><Camera /></el-icon>
                </div>
              </el-upload>
              <div class="avatar-tip">支持 jpg、png 格式，不超过 5MB</div>
            </el-form-item>
            
            <el-form-item label="群名称" v-if="canEditGroupInfo()">
              <el-input v-model="groupSettings.groupName" style="width: 300px" />
              <el-button type="primary" @click="handleUpdateGroupName" style="margin-left: 10px">修改</el-button>
            </el-form-item>
            <el-form-item label="群主" v-if="currentGroup">
              <span>{{ getOwnerName() }}</span>
            </el-form-item>
            <el-form-item v-if="currentGroup?.creatorId === userStore.userInfo?.id">
              <el-button type="danger" @click="handleDismissGroup">解散群组</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Camera } from '@element-plus/icons-vue'
import { getUserGroups, searchGroups, createGroup, joinGroup, getGroupMembers, removeMember, setAdmin, transferOwner, dismissGroup, updateGroupInfo, uploadGroupAvatar } from '../api/group'
import { deleteGroupMessages } from '../api/message'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const groupList = ref([])
const showCreateDialog = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const createForm = ref({
  groupName: ''
})

// 群管理相关
const showManageDialogVisible = ref(false)
const currentGroup = ref(null)
const manageTab = ref('members')
const memberList = ref([])
const groupSettings = ref({
  groupName: ''
})
const groupAvatarPreview = ref('')

const loadGroups = async () => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  console.log('开始加载群组列表，用户ID:', userStore.userInfo.id)
  
  try {
    const res = await getUserGroups(userStore.userInfo.id)
    console.log('获取群组列表成功:', res)
    console.log('群组数据:', res.data)
    console.log('群组数量:', res.data?.length || 0)
    groupList.value = res.data || []
    
    if (groupList.value.length === 0) {
      console.log('当前用户没有加入任何群组')
    }
  } catch (error) {
    console.error('加载群组列表失败:', error)
    console.error('错误详情:', error.response || error.message)
    ElMessage.error('加载群组列表失败: ' + (error.response?.data?.message || error.message))
    groupList.value = []
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value) {
    searchResults.value = []
    return
  }
  
  try {
    const res = await searchGroups(searchKeyword.value)
    searchResults.value = res.data
    
    if (searchResults.value.length === 0) {
      ElMessage.info('未找到匹配的群组')
    }
  } catch (error) {
    console.error(error)
  }
}

const handleCreate = async () => {
  if (!createForm.value.groupName) {
    ElMessage.warning('请输入群组名称')
    return
  }
  
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  try {
    const res = await createGroup(createForm.value.groupName, userStore.userInfo.id)
    ElMessage.success('群组创建成功')
    showCreateDialog.value = false
    createForm.value.groupName = ''
    // 重新加载群组列表，确保显示最新数据
    await loadGroups()
  } catch (error) {
    console.error('创建群组失败:', error)
    ElMessage.error(error.message || '创建群组失败')
  }
}

const handleJoinGroup = async (group) => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.error('用户信息未加载')
    return
  }
  
  try {
    await joinGroup(group.groupId, userStore.userInfo.id)
    ElMessage.success('申请已提交，待申请通过')
    loadGroups()
  } catch (error) {
    console.error(error)
  }
}

const enterGroup = (group) => {
  console.log('进入群组:', group)
  // 跳转到聊天页面并选中该群组
  router.push({ 
    name: 'Chat',
    query: { 
      groupId: group.groupId,
      groupName: group.groupName,
      type: 'group'
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 显示管理对话框
const showManageDialog = async (group) => {
  currentGroup.value = group
  groupSettings.value.groupName = group.groupName
  showManageDialogVisible.value = true
  await loadMembers(group.groupId)
}

// 加载群成员
const loadMembers = async (groupId) => {
  try {
    const res = await getGroupMembers(groupId)
    memberList.value = res.data || []
    console.log('群成员列表:', memberList.value)
  } catch (error) {
    console.error('加载群成员失败:', error)
    ElMessage.error('加载群成员失败')
  }
}

// 获取头像文字（取昵称的第一个字）
const getAvatarText = (member) => {
  if (!member) return '?'
  const nickname = member.nickname || member.username || member.account || '用户'
  return nickname.charAt(0)
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  const typeMap = {
    0: 'info',      // 普通成员
    1: 'warning',   // 管理员
    2: 'danger'     // 群主
  }
  return typeMap[role] || 'info'
}

// 判断是否可以管理该成员
const canManageMember = (memberUserId) => {
  if (!currentGroup.value) return false
  // 群主可以管理所有成员（除了自己）
  if (currentGroup.value.creatorId === userStore.userInfo?.id) {
    return memberUserId !== userStore.userInfo?.id
  }
  // 管理员不能管理其他管理员或群主
  return false
}

// 判断当前用户是否是管理员
const isCurrentUserAdmin = () => {
  if (!currentGroup.value || !memberList.value.length) return false
  const currentMember = memberList.value.find(m => m.userId === userStore.userInfo?.id)
  return currentMember && currentMember.role === 1
}

// 判断当前用户是否可以修改群信息（群主或管理员）
const canEditGroupInfo = () => {
  if (!currentGroup.value || !memberList.value.length) return false
  const currentMember = memberList.value.find(m => m.userId === userStore.userInfo?.id)
  // role: 0-普通成员, 1-管理员, 2-群主
  return currentMember && (currentMember.role === 1 || currentMember.role === 2)
}

// 获取群主名称
const getOwnerName = () => {
  if (!memberList.value.length) return '未知'
  const owner = memberList.value.find(m => m.role === 2)
  return owner ? `${owner.nickname} (${owner.account})` : '未知'
}

// 设置管理员
const handleSetAdmin = async (targetUserId, isAdmin) => {
  try {
    await ElMessageBox.confirm(
      `确定${isAdmin ? '设为' : '取消'}该用户的管理员身份吗？`,
      '提示'
    )
    await setAdmin(currentGroup.value.groupId, userStore.userInfo.id, targetUserId, isAdmin)
    ElMessage.success('操作成功')
    await loadMembers(currentGroup.value.groupId)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置管理员失败:', error)
    }
  }
}

// 转让群主
const handleTransferOwner = async (newOwnerId) => {
  try {
    await ElMessageBox.confirm(
      '转让群主后，您将成为普通成员，此操作不可撤销，确定继续吗？',
      '警告',
      { type: 'warning' }
    )
    await transferOwner(currentGroup.value.groupId, userStore.userInfo.id, newOwnerId)
    ElMessage.success('转让成功')
    showManageDialogVisible.value = false
    await loadGroups()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('转让群主失败:', error)
    }
  }
}

// 移除成员
const handleRemoveMember = async (targetUserId) => {
  try {
    const member = memberList.value.find(m => m.userId === targetUserId)
    await ElMessageBox.confirm(
      `确定要移除成员 ${member?.nickname || '该用户'} 吗？`,
      '提示'
    )
    await removeMember(currentGroup.value.groupId, userStore.userInfo.id, targetUserId)
    ElMessage.success('移除成功')
    await loadMembers(currentGroup.value.groupId)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
    }
  }
}

// 修改群名称
const handleUpdateGroupName = async () => {
  if (!groupSettings.value.groupName.trim()) {
    ElMessage.warning('群名称不能为空')
    return
  }
  
  try {
    await updateGroupInfo(
      currentGroup.value.groupId,
      userStore.userInfo.id,
      groupSettings.value.groupName,
      null
    )
    ElMessage.success('修改成功')
    currentGroup.value.groupName = groupSettings.value.groupName
    await loadGroups()
  } catch (error) {
    console.error('修改群名称失败:', error)
  }
}

// 解散群组
const handleDismissGroup = async () => {
  try {
    await ElMessageBox.confirm(
      '解散群组后，所有数据将被删除（包括聊天记录），此操作不可恢复，确定继续吗？',
      '严重警告',
      { type: 'error' }
    )
    
    // 解散群组（后端会自动清空聊天记录）
    await dismissGroup(currentGroup.value.groupId, userStore.userInfo.id)
    ElMessage.success('群组已解散')
    showManageDialogVisible.value = false
    await loadGroups()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解散群组失败:', error)
    }
  }
}

// 获取群头像URL
const getGroupAvatarUrl = (avatar) => {
  if (!avatar) return ''
  const config = userStore.serverConfig
  const baseUrl = config?.baseUrl || 'http://localhost:8080'
  if (avatar.startsWith('http')) return avatar
  return `${baseUrl}/${avatar}`
}

// 获取用户头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  const config = userStore.serverConfig
  const baseUrl = config?.baseUrl || 'http://localhost:8080'
  return avatar.startsWith('http') ? avatar : `${baseUrl}/${avatar}`
}

// 上传前验证
const beforeGroupAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  
  // 预览图片
  groupAvatarPreview.value = URL.createObjectURL(file)
  return true
}

// 上传群头像
const handleUploadGroupAvatar = async (options) => {
  const { file } = options
  
  // 保存当前群组ID,防止刷新后丢失
  const savedGroupId = currentGroup.value?.groupId
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('groupId', currentGroup.value.groupId)
    formData.append('operatorId', userStore.userInfo.id)
    
    const res = await uploadGroupAvatar(formData)
    ElMessage.success('群头像上传成功')
    
    // 更新本地群组信息
    const avatarPath = res.data
    
    // 清除预览
    groupAvatarPreview.value = ''
    
    // 刷新群组列表(从后端获取最新数据)
    await loadGroups()
    
    // 从最新的群组列表中找到当前群组,并更新currentGroup
    if (savedGroupId) {
      const updatedGroup = groupList.value.find(g => g.groupId === savedGroupId)
      if (updatedGroup) {
        currentGroup.value = updatedGroup
        console.log('头像上传成功,已更新群组信息:', updatedGroup)
      }
    }
  } catch (error) {
    console.error('上传群头像失败:', error)
    ElMessage.error(error.message || '上传群头像失败')
    groupAvatarPreview.value = ''
  }
}

onMounted(() => {
  loadGroups()
})
</script>

<style scoped>
.groups-container {
  padding: 20px;
  height: 100%;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
  color: #303133;
}

.search-results {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.my-groups {
  margin-top: 20px;
}

.group-item {
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.group-item:hover {
  transform: translateY(-2px);
}

.group-item-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
}

.group-detail {
  flex: 1;
}

.group-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.group-id,
.group-time {
  font-size: 13px;
  color: #909399;
}

/* 群管理相关样式 */
.member-list {
  max-height: 500px;
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.member-item:hover {
  background: #e8eaed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.member-info {
  flex: 1;
  margin-left: 15px;
}

.member-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 5px;
  color: #303133;
}

.member-time {
  font-size: 12px;
  color: #909399;
}

.member-actions {
  margin-left: 15px;
}

/* 群头像上传样式 */
.group-avatar-uploader {
  position: relative;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  display: inline-block;
}

.group-avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}
</style>