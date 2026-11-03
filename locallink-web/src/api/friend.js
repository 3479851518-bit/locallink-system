import request from '../utils/request'

export function searchUser(keyword) {
  return request({
    url: '/friend/search',
    method: 'get',
    params: { keyword }
  })
}

export function addFriend(data) {
  return request({
    url: '/friend/add',
    method: 'post',
    params: data
  })
}

export function acceptFriend(relationId) {
  return request({
    url: '/friend/accept',
    method: 'post',
    params: { relationId }
  })
}

export function rejectFriend(relationId) {
  return request({
    url: '/friend/reject',
    method: 'post',
    params: { relationId }
  })
}

export function getFriendList(userId) {
  return request({
    url: `/friend/list/${userId}`,
    method: 'get'
  })
}

export function getPendingRequests(userId) {
  return request({
    url: `/friend/pending/${userId}`,
    method: 'get'
  })
}

export function removeFriend(userId, friendId) {
  return request({
    url: '/friend/remove',
    method: 'post',
    params: { userId, friendId }
  })
}

/**
 * 修改好友备注
 */
export function updateRemark(userId, friendId, remark) {
  return request({
    url: '/friend/update-remark',
    method: 'post',
    params: { userId, friendId, remark }
  })
}

/**
 * 获取好友详情
 */
export function getFriendDetail(userId, friendId) {
  return request({
    url: `/friend/detail/${userId}/${friendId}`,
    method: 'get'
  })
}

/**
 * 拉黑好友
 */
export function blockFriend(userId, friendId) {
  return request({
    url: '/friend/block',
    method: 'post',
    params: { userId, friendId }
  })
}

/**
 * 取消拉黑
 */
export function unblockFriend(userId, friendId) {
  return request({
    url: '/friend/unblock',
    method: 'post',
    params: { userId, friendId }
  })
}
