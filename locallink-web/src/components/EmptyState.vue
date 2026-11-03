<template>
  <div class="empty-state">
    <el-empty 
      :image="image" 
      :image-size="imageSize"
      :description="description"
    >
      <slot>
        <el-button v-if="showButton" type="primary" @click="handleClick">
          {{ buttonText }}
        </el-button>
      </slot>
    </el-empty>
  </div>
</template>

<script setup>
const props = defineProps({
  image: {
    type: String,
    default: ''
  },
  imageSize: {
    type: Number,
    default: 160
  },
  description: {
    type: String,
    default: '暂无数据'
  },
  showButton: {
    type: Boolean,
    default: false
  },
  buttonText: {
    type: String,
    default: '去添加'
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}
</script>

<style scoped>
.empty-state {
  padding: 40px 20px;
}
</style>
