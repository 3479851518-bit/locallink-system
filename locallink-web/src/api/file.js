import request from '../utils/request'

export function uploadFile(data) {
  return request({
    url: '/file/upload',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadFile(taskId) {
  return request({
    url: `/file/download/${taskId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function getProgress(taskId) {
  return request({
    url: `/file/progress/${taskId}`,
    method: 'get'
  })
}
