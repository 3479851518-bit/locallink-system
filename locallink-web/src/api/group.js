import request from '../utils/request'

export function getUserGroups(userId) {
  return request({
    url: `/group/list/${userId}`,
    method: 'get'
  })
}

export function searchGroups(keyword) {
  return request({
    url: '/group/search',
    method: 'get',
    params: { keyword }
  })
}

export function createGroup(groupName, creatorId) {
  return request({
    url: '/group/create',
    method: 'post',
    params: { groupName, creatorId }
  })
}

export function joinGroup(groupId, userId) {
  return request({
    url: '/group/join',
    method: 'get',
    params: { groupId, userId }
  })
}

/**
 * 通过入群申请
 */
export function acceptApply(applyId, operatorId) {
  return request({
    url: '/group/accept-apply',
    method: 'post',
    params: { applyId, operatorId }
  })
}

/**
 * 拒绝入群申请
 */
export function rejectApply(applyId, operatorId) {
  return request({
    url: '/group/reject-apply',
    method: 'post',
    params: { applyId, operatorId }
  })
}

/**
 * 获取待审核申请列表
 */
export function getPendingApplies(groupId) {
  return request({
    url: `/group/pending-applies/${groupId}`,
    method: 'get'
  })
}

export function quitGroup(groupId, userId) {
  return request({
    url: '/group/quit',
    method: 'get',
    params: { groupId, userId }
  })
}

/**
 * 获取群成员列表
 */
export function getGroupMembers(groupId) {
  return request({
    url: `/group/members/${groupId}`,
    method: 'get'
  })
}

/**
 * 移除群成员
 */
export function removeMember(groupId, operatorId, targetUserId) {
  return request({
    url: '/group/remove-member',
    method: 'post',
    params: { groupId, operatorId, targetUserId }
  })
}

/**
 * 设置管理员
 */
export function setAdmin(groupId, operatorId, targetUserId, isAdmin) {
  return request({
    url: '/group/set-admin',
    method: 'post',
    params: { groupId, operatorId, targetUserId, isAdmin }
  })
}

/**
 * 转让群主
 */
export function transferOwner(groupId, currentOwnerId, newOwnerId) {
  return request({
    url: '/group/transfer-owner',
    method: 'post',
    params: { groupId, currentOwnerId, newOwnerId }
  })
}

/**
 * 解散群组
 */
export function dismissGroup(groupId, ownerId) {
  return request({
    url: '/group/dismiss',
    method: 'post',
    params: { groupId, ownerId }
  })
}

/**
 * 修改群信息
 */
export function updateGroupInfo(groupId, operatorId, groupName, groupAvatar) {
  return request({
    url: '/group/update',
    method: 'post',
    params: { groupId, operatorId, groupName, groupAvatar }
  })
}

/**
 * 上传群头像
 */
export function uploadGroupAvatar(formData) {
  return request({
    url: '/group/upload-avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
