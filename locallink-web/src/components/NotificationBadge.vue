<template>
  <el-badge 
    :value="count" 
    :max="max"
    :hidden="count === 0"
    :dot="isDot"
  >
    <slot>
      <el-icon :size="size">
        <component :is="icon" />
      </el-icon>
    </slot>
  </el-badge>
</template>

<script setup>
const props = defineProps({
  count: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: 99
  },
  isDot: {
    type: Boolean,
    default: false
  },
  size: {
    type: [Number, String],
    default: 20
  },
  icon: {
    type: [String, Object],
    default: 'Bell'
  }
})
</script>
