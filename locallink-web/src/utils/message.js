import { ElMessage } from 'element-plus'

// 用于跟踪当前显示的错误消息
let currentErrorInstance = null

/**
 * 显示错误消息（确保同一时间只显示一个错误弹窗）
 * @param {string} message - 错误消息内容
 * @param {object} options - ElMessage 配置选项
 */
export function showErrorOnce(message, options = {}) {
  // 如果已有错误消息显示，先关闭它
  if (currentErrorInstance) {
    currentErrorInstance.close()
  }
  
  // 显示新的错误消息
  currentErrorInstance = ElMessage({
    message,
    type: 'error',
    duration: options.duration || 3000,
    showClose: options.showClose !== undefined ? options.showClose : true,
    onClose: () => {
      currentErrorInstance = null
    },
    ...options
  })
  
  return currentErrorInstance
}

/**
 * 显示警告消息（确保同一时间只显示一个警告弹窗）
 */
export function showWarningOnce(message, options = {}) {
  // 如果已有警告消息显示，先关闭它
  if (currentErrorInstance) {
    currentErrorInstance.close()
  }
  
  currentErrorInstance = ElMessage({
    message,
    type: 'warning',
    duration: options.duration || 3000,
    showClose: options.showClose !== undefined ? options.showClose : true,
    onClose: () => {
      currentErrorInstance = null
    },
    ...options
  })
  
  return currentErrorInstance
}

/**
 * 显示成功消息
 */
export function showSuccessOnce(message, options = {}) {
  if (currentErrorInstance) {
    currentErrorInstance.close()
  }
  
  currentErrorInstance = ElMessage({
    message,
    type: 'success',
    duration: options.duration || 2000,
    showClose: options.showClose !== undefined ? options.showClose : false,
    onClose: () => {
      currentErrorInstance = null
    },
    ...options
  })
  
  return currentErrorInstance
}
