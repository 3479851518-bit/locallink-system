<template>
  <div class="message-bubble" :class="isMyMessage ? 'my-message' : 'other-message'">
    <UserAvatar 
      :text="senderName" 
      :status="senderStatus"
      :show-status="showStatus"
      :size="avatarSize"
    />
    <div class="message-content">
      <div v-if="showSender" class="sender-name">{{ senderName }}</div>
      <div class="bubble">
        <slot>{{ content }}</slot>
      </div>
      <div v-if="showTime" class="message-time">{{ formatTime(time) }}</div>
    </div>
  </div>
</template>

<script setup>
import UserAvatar from './UserAvatar.vue'

const props = defineProps({
  isMyMessage: {
    type: Boolean,
    default: false
  },
  senderName: {
    type: String,
    default: ''
  },
  senderStatus: {
    type: Number,
    default: 0
  },
  showStatus: {
    type: Boolean,
    default: false
  },
  avatarSize: {
    type: [Number, String],
    default: 36
  },
  showSender: {
    type: Boolean,
    default: false
  },
  showTime: {
    type: Boolean,
    default: true
  },
  time: {
    type: [String, Date],
    default: ''
  },
  content: {
    type: String,
    default: ''
  }
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}
</script>

<style scoped>
.message-bubble {
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
}

.bubble {
  background-color: #f5f7fa;
  padding: 8px 12px;
  border-radius: 8px;
  word-wrap: break-word;
  line-height: 1.5;
}

.my-message .bubble {
  background-color: #409eff;
  color: #fff;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  text-align: right;
}

.other-message .message-time {
  text-align: left;
}
</style>
