import request from '../utils/request'

export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    transformRequest: [function (data) {
      let ret = ''
      for (let it in data) {
        ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
      }
      ret = ret.substring(0, ret.lastIndexOf('&'))
      return ret
    }]
  })
}

export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export function getUserInfo(userId) {
  return request({
    url: `/user/info/${userId}`,
    method: 'get'
  })
}

export function updateStatus(data) {
  return request({
    url: '/user/status',
    method: 'post',
    params: data
  })
}

/**
 * 上传头像
 */
export function uploadAvatar(formData) {
  return request({
    url: '/user/upload-avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 修改昵称
 */
export function updateNickname(userId, nickname) {
  return request({
    url: '/user/update-nickname',
    method: 'post',
    params: { userId, nickname }
  })
}

/**
 * 修改头像（URL方式）
 */
export function updateAvatar(userId, avatar) {
  return request({
    url: '/user/update-avatar',
    method: 'post',
    params: { userId, avatar }
  })
}

/**
 * 修改密码
 */
export function updatePassword(userId, oldPassword, newPassword) {
  return request({
    url: '/user/update-password',
    method: 'post',
    params: { userId, oldPassword, newPassword }
  })
}

/**
 * 获取服务器配置
 */
export function getServerConfig() {
  return request({
    url: '/user/config',
    method: 'get'
  })
}
