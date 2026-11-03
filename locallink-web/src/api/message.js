import request from '../utils/request'

export function sendMessage(data) {
  return request({
    url: '/message/send',
    method: 'post',
    data
  })
}

export function getHistory(params) {
  return request({
    url: '/message/history',
    method: 'get',
    params
  })
}

export function getGroupHistory(groupId, limit = 50, userId = null) {
  const params = { limit }
  if (userId !== null) {
    params.userId = userId
  }
  return request({
    url: `/message/group/${groupId}`,
    method: 'get',
    params
  })
}

/**
 * 撤回消息
 */
export function recallMessage(messageId, userId) {
  return request({
    url: '/message/recall',
    method: 'post',
    params: { messageId, userId }
  })
}

/**
 * 删除私聊消息记录
 */
export function deletePrivateMessages(userId, friendId) {
  return request({
    url: '/message/delete-private',
    method: 'post',
    params: { userId, friendId }
  })
}

/**
 * 删除群聊消息记录
 */
export function deleteGroupMessages(groupId) {
  return request({
    url: '/message/delete-group',
    method: 'post',
    params: { groupId }
  })
}

/**
 * 删除单条消息
 */
export function deleteMessage(messageId, userId) {
  return request({
    url: '/message/delete',
    method: 'post',
    params: { messageId, userId }
  })
}
