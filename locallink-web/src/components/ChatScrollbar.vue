<template>
  <el-scrollbar 
    ref="scrollbarRef"
    class="chat-scrollbar"
    @scroll="handleScroll"
  >
    <div class="scroll-container">
      <div v-if="loading" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <slot></slot>
    </div>
  </el-scrollbar>
</template>

<script setup>
import { ref } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  threshold: {
    type: Number,
    default: 50
  }
})

const emit = defineEmits(['load-more', 'scroll'])

const scrollbarRef = ref(null)

const handleScroll = ({ scrollTop, scrollLeft }) => {
  emit('scroll', { scrollTop, scrollLeft })
  
  if (scrollTop < props.threshold && !props.loading) {
    emit('load-more')
  }
}

const scrollToBottom = () => {
  const scrollbar = scrollbarRef.value?.wrapRef
  if (scrollbar) {
    setTimeout(() => {
      scrollbar.scrollTop = scrollbar.scrollHeight
    }, 100)
  }
}

const scrollToTop = () => {
  const scrollbar = scrollbarRef.value?.wrapRef
  if (scrollbar) {
    scrollbar.scrollTop = 0
  }
}

defineExpose({
  scrollToBottom,
  scrollToTop
})
</script>

<style scoped>
.chat-scrollbar {
  height: 100%;
}

.scroll-container {
  padding: 20px;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  color: #909399;
  font-size: 14px;
}

.loading-more .el-icon {
  margin-right: 8px;
}
</style>
