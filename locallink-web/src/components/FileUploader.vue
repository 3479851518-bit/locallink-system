<template>
  <el-upload
    ref="uploadRef"
    class="file-uploader"
    :auto-upload="false"
    :on-change="handleFileChange"
    :before-upload="beforeUpload"
    :limit="1"
    :file-list="fileList"
  >
    <el-button type="primary">
      <el-icon><UploadFilled /></el-icon>
      {{ buttonLabel }}
    </el-button>
  </el-upload>
  
  <div v-if="selectedFile" class="file-info">
    <el-icon><Document /></el-icon>
    <span class="file-name">{{ selectedFile.name }}</span>
    <span class="file-size">{{ formatSize(selectedFile.size) }}</span>
    <el-button type="danger" size="small" text @click="clearFile">
      <el-icon><Delete /></el-icon>
    </el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Document, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  buttonLabel: {
    type: String,
    default: '选择文件'
  },
  maxSize: {
    type: Number,
    default: 100 * 1024 * 1024
  },
  accept: {
    type: String,
    default: '*'
  }
})

const emit = defineEmits(['change', 'clear'])

const uploadRef = ref(null)
const fileList = ref([])
const selectedFile = ref(null)

const handleFileChange = (file) => {
  if (file.size > props.maxSize) {
    ElMessage.error(`文件大小不能超过 ${formatSize(props.maxSize)}`)
    fileList.value = []
    return
  }
  
  selectedFile.value = file.raw
  emit('change', file.raw)
}

const beforeUpload = (file) => {
  return file.size <= props.maxSize
}

const clearFile = () => {
  fileList.value = []
  selectedFile.value = null
  emit('clear')
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

defineExpose({
  clear: clearFile,
  getFile: () => selectedFile.value
})
</script>

<style scoped>
.file-uploader {
  display: inline-block;
}

.file-info {
  display: flex;
  align-items: center;
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.file-info .el-icon {
  font-size: 20px;
  margin-right: 10px;
  color: #409eff;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  color: #909399;
  font-size: 12px;
  margin: 0 10px;
}
</style>
