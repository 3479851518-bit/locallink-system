import request from '../utils/request'

/**
 * 获取所有用户列表
 */
export function getAllUsers() {
  return request({
    url: '/admin/users',
    method: 'get'
  })
}

/**
 * 封号用户
 */
export function banUser(userId, adminId, reason) {
  return request({
    url: '/admin/ban-user',
    method: 'post',
    params: { userId, adminId, reason }
  })
}

/**
 * 解封用户
 */
export function unbanUser(userId, adminId) {
  return request({
    url: '/admin/unban-user',
    method: 'post',
    params: { userId, adminId }
  })
}

/**
 * 禁言用户
 */
export function muteUser(userId, adminId, reason, durationMinutes) {
  return request({
    url: '/admin/mute-user',
    method: 'post',
    params: { userId, adminId, reason, durationMinutes }
  })
}

/**
 * 解除禁言
 */
export function unmuteUser(userId, adminId) {
  return request({
    url: '/admin/unmute-user',
    method: 'post',
    params: { userId, adminId }
  })
}

/**
 * 设置用户角色
 */
export function setUserRole(userId, adminId, role) {
  return request({
    url: '/admin/set-role',
    method: 'post',
    params: { userId, adminId, role }
  })
}

/**
 * 提交举报
 */
export function submitReport(data) {
  return request({
    url: '/admin/report',
    method: 'post',
    params: data
  })
}

/**
 * 获取待处理的举报列表
 */
export function getPendingReports() {
  return request({
    url: '/admin/reports/pending',
    method: 'get'
  })
}

/**
 * 获取所有举报列表
 */
export function getAllReports() {
  return request({
    url: '/admin/reports/all',
    method: 'get'
  })
}

/**
 * 处理举报
 */
export function handleReport(reportId, handlerId, status, handleResult) {
  return request({
    url: '/admin/report/handle',
    method: 'post',
    params: { reportId, handlerId, status, handleResult }
  })
}

/**
 * 处理举报并执行处罚
 */
export function handleReportWithPenalty(reportId, handlerId, status, handleResult, penaltyType) {
  return request({
    url: '/admin/report/handle-with-penalty',
    method: 'post',
    params: { reportId, handlerId, status, handleResult, penaltyType }
  })
}

/**
 * 提交申诉
 */
export function submitAppeal(userId, content) {
  return request({
    url: '/admin/appeal',
    method: 'post',
    params: { userId, content }
  })
}

/**
 * 获取待处理的申诉列表
 */
export function getPendingAppeals() {
  return request({
    url: '/admin/appeals/pending',
    method: 'get'
  })
}
