<template>
  <div class="user-avatar" :style="{ width: size + 'px', height: size + 'px' }">
    <el-avatar 
      :size="size" 
      :src="src"
      :style="{ backgroundColor: avatarColor }"
    >
      {{ text }}
    </el-avatar>
    <span 
      v-if="showStatus" 
      class="status-dot" 
      :class="`status-${status}`"
    ></span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  text: {
    type: String,
    default: ''
  },
  size: {
    type: [Number, String],
    default: 40
  },
  showStatus: {
    type: Boolean,
    default: false
  },
  status: {
    type: Number,
    default: 0
  }
})

const avatarColor = computed(() => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  const index = props.text.charCodeAt(0) % colors.length
  return colors[index]
})
</script>

<style scoped>
.user-avatar {
  position: relative;
  display: inline-block;
}

.status-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #fff;
}

.status-0 {
  background-color: #909399;
}

.status-1 {
  background-color: #67c23a;
}

.status-2 {
  background-color: #e6a23c;
}
</style>
