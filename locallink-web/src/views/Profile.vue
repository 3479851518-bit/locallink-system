<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      
      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleUploadAvatar"
            accept="image/*"
          >
            <el-avatar :size="120" :src="avatarPreview || userInfo.avatar || ''">
              {{ userInfo.nickname?.[0] || '用户' }}
            </el-avatar>
            <div class="avatar-overlay">
              <el-icon :size="24"><Camera /></el-icon>
              <div>点击上传</div>
            </div>
          </el-upload>
          <div class="avatar-tip">支持 jpg、png 格式，不超过 5MB</div>
        </div>
        
        <!-- 信息表单 -->
        <el-form :model="profileForm" label-width="100px" class="info-form">
          <el-form-item label="账号">
            <el-input v-model="userInfo.account" disabled />
          </el-form-item>
          
          <el-form-item label="昵称">
            <div style="display: flex; gap: 10px; align-items: center;">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" style="width: 300px" />
              <el-button type="primary" @click="handleUpdateNickname">修改</el-button>
            </div>
          </el-form-item>
          
          <el-form-item label="用户名">
            <el-input v-model="userInfo.username" disabled />
          </el-form-item>
          
          <el-divider />
          
          <el-form-item label="修改密码">
            <el-button type="warning" @click="showPasswordDialog = true">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="450px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="原密码">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { updateNickname, uploadAvatar, updatePassword } from '../api/user'

const userStore = useUserStore()
const userInfo = ref({})
const avatarPreview = ref('')

// 表单数据
const profileForm = reactive({
  nickname: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 对话框显示状态
const showPasswordDialog = ref(false)

// 加载用户信息
const loadUserInfo = () => {
  if (userStore.userInfo) {
    userInfo.value = { ...userStore.userInfo }
    profileForm.nickname = userStore.userInfo.nickname || ''
    // 如果有头像，转换为完整URL
    if (userStore.userInfo.avatar && !userStore.userInfo.avatar.startsWith('http')) {
      const config = userStore.serverConfig
      const baseUrl = config?.baseUrl || 'http://localhost:8080'
      userInfo.value.avatar = `${baseUrl}/${userStore.userInfo.avatar}`
    }
  }
}

// 修改昵称
const handleUpdateNickname = async () => {
  if (!profileForm.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  
  try {
    await updateNickname(userStore.userInfo.id, profileForm.nickname)
    ElMessage.success('昵称修改成功')
    
    // 更新本地用户信息
    userInfo.value.nickname = profileForm.nickname
    userStore.userInfo.nickname = profileForm.nickname
    
    // 刷新页面以更新显示
    window.location.reload()
  } catch (error) {
    console.error('修改昵称失败:', error)
    ElMessage.error(error.message || '修改昵称失败')
  }
}

// 上传前验证
const beforeAvatarUpload = (file) => {
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
  avatarPreview.value = URL.createObjectURL(file)
  return true
}

// 上传头像
const handleUploadAvatar = async (options) => {
  const { file } = options
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('userId', userStore.userInfo.id)
    
    const res = await uploadAvatar(formData)
    ElMessage.success('头像上传成功')
    
    // 更新本地用户信息（转换为完整URL）
    const avatarPath = res.data
    const config = userStore.serverConfig
    const baseUrl = config?.baseUrl || 'http://localhost:8080'
    const avatarUrl = avatarPath.startsWith('http') ? avatarPath : `${baseUrl}/${avatarPath}`
    userInfo.value.avatar = avatarUrl
    userStore.userInfo.avatar = avatarPath  // 存储相对路径
    
    // 清除预览
    avatarPreview.value = ''
    
    // 不需要刷新页面，头像已实时更新
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error(error.message || '上传头像失败')
    avatarPreview.value = ''
  }
}

// 修改密码
const handleUpdatePassword = async () => {
  // 验证表单
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  try {
    await updatePassword(
      userStore.userInfo.id,
      passwordForm.oldPassword,
      passwordForm.newPassword
    )
    
    ElMessage.success('密码修改成功，请重新登录')
    showPasswordDialog.value = false
    
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
    // 退出登录
    setTimeout(() => {
      userStore.logout()
      window.location.href = '/login'
    }, 1500)
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败')
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.profile-card {
  width: 100%;
  max-width: 700px;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
}

.profile-content {
  display: flex;
  gap: 40px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 150px;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-uploader:hover .avatar-overlay {
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
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  gap: 5px;
}

.avatar-overlay div {
  font-size: 12px;
}

.avatar-tip {
  margin-top: 15px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.info-form {
  flex: 1;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
