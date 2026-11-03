import request from '../utils/request'

// 获取通知列表
export function getNotifications(userId, type) {
  return request({
    url: '/notification/list',
    method: 'get',
    params: { userId, type }
  })
}

// 获取未读通知数量
export function getUnreadCount(userId) {
  return request({
    url: '/notification/unread-count',
    method: 'get',
    params: { userId }
  })
}

// 标记通知为已读
export function markAsRead(notificationId) {
  return request({
    url: `/notification/mark-read/${notificationId}`,
    method: 'post'
  })
}

// 全部标记为已读
export function markAllAsRead(userId) {
  return request({
    url: '/notification/mark-all-read',
    method: 'post',
    params: { userId }
  })
}
