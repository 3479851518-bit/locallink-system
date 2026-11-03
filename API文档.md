# LocalLink API 文档

## 基础信息
- 基础 URL: `http://localhost:8080/api`
- 响应格式: `{"code": 200, "message": "success", "data": {}}`
- 认证方式: 用户ID

## 接口列表

### 用户管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/user/register` | POST | 用户注册 |
| `/user/login` | POST | 用户登录 |
| `/user/info/{userId}` | GET | 获取用户信息 |
| `/user/status` | POST | 更新在线状态 |
| `/user/upload-avatar` | POST | 上传头像 |
| `/user/update-nickname` | POST | 修改昵称 |
| `/user/update-password` | POST | 修改密码 |

### 好友系统
| 接口 | 方法 | 说明 |
|------|------|------|
| `/friend/add` | POST | 添加好友 |
| `/friend/accept` | POST | 接受好友申请 |
| `/friend/reject` | POST | 拒绝好友申请 |
| `/friend/list/{userId}` | GET | 获取好友列表 |
| `/friend/search` | GET | 搜索用户 |
| `/friend/pending/{userId}` | GET | 获取待处理申请 |
| `/friend/remove` | POST | 删除好友 |
| `/friend/update-remark` | POST | 修改备注 |
| `/friend/block` | POST | 拉黑好友 |
| `/friend/unblock` | POST | 取消拉黑 |

### 群组管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/group/create` | POST | 创建群组 |
| `/group/upload-avatar` | POST | 上传群头像 |
| `/group/join` | GET | 申请加入群组 |
| `/group/accept-apply` | POST | 通过入群申请 |
| `/group/reject-apply` | POST | 拒绝入群申请 |
| `/group/pending-applies/{groupId}` | GET | 获取待审核申请 |
| `/group/quit` | GET | 退出群组 |
| `/group/list/{userId}` | GET | 获取用户群组列表 |
| `/group/info/{groupId}` | GET | 获取群组信息 |
| `/group/search` | GET | 搜索群组 |
| `/group/members/{groupId}` | GET | 获取群成员列表 |
| `/group/remove-member` | POST | 移除群成员 |
| `/group/set-admin` | POST | 设置管理员 |
| `/group/transfer-owner` | POST | 转让群主 |
| `/group/dismiss` | POST | 解散群组 |
| `/group/update` | POST | 修改群信息 |

### 消息系统
| 接口 | 方法 | 说明 |
|------|------|------|
| `/message/send` | POST | 发送消息 |
| `/message/history` | GET | 获取私聊历史 |
| `/message/group/{groupId}` | GET | 获取群聊历史 |
| `/message/recall` | POST | 撤回消息 |
| `/message/delete-private` | POST | 删除私聊记录 |
| `/message/delete-group` | POST | 删除群聊记录 |
| `/message/delete` | POST | 删除单条消息 |

### 文件传输
| 接口 | 方法 | 说明 |
|------|------|------|
| `/file/upload` | POST | 上传文件 |
| `/file/download/{taskId}` | GET | 下载文件 |
| `/file/progress/{taskId}` | GET | 获取传输进度 |

### 管理员功能
| 接口 | 方法 | 说明 |
|------|------|------|
| `/admin/users` | GET | 获取所有用户 |
| `/admin/ban-user` | POST | 封号用户 |
| `/admin/unban-user` | POST | 解封用户 |
| `/admin/mute-user` | POST | 禁言用户 |
| `/admin/unmute-user` | POST | 解除禁言 |
| `/admin/set-role` | POST | 设置用户角色 |
| `/admin/report` | POST | 提交举报 |
| `/admin/reports/pending` | GET | 获取待处理举报 |
| `/admin/report/handle` | POST | 处理举报 |
| `/admin/appeal` | POST | 提交申诉 |
| `/admin/appeals/pending` | GET | 获取待处理申诉 |

### 通知管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/notification/list` | GET | 获取通知列表 |
| `/notification/unread-count` | GET | 获取未读通知数量 |
| `/notification/mark-read/{notificationId}` | POST | 标记通知为已读 |
| `/notification/mark-all-read` | POST | 全部标记为已读 |

## WebSocket
连接地址: `ws://localhost:8080/ws/chat/{userId}`

### 消息类型
| 类型 | 说明 | 数据格式 |
|------|------|----------|
| `chat` | 聊天消息 | 包含 senderId, receiverId, content, messageType 等 |
| `notification` | 系统通知 | 包含 id, userId, type, title, content, isRead 等 |
| `online` | 上线通知 | 用户上线时推送 |
| `offline` | 下线通知 | 用户下线时推送 |
| `heartbeat` | 心跳消息 | 保持连接活跃 |
| `error` | 错误消息 | 操作失败时返回 |

### 通知消息示例
```json
{
  "type": "notification",
  "data": {
    "id": 1,
    "userId": 123,
    "type": 1,
    "title": "好友申请",
    "content": "张三申请添加您为好友",
    "relatedId": 456,
    "isRead": 0,
    "createTime": "2026-04-28T10:00:00"
  }
}
```

## 枚举值
- **用户角色**: 0-普通用户, 1-管理员
- **在线状态**: 0-离线, 1-在线, 2-忙碌
- **聊天类型**: 1-私聊, 2-群聊
- **消息类型**: 1-文本, 2-图片, 3-文件, 4-语音
- **群角色**: 0-成员, 1-管理员, 2-群主
- **举报类型**: 1-用户举报, 2-消息举报
- **举报状态**: 0-待处理, 1-已处理, 2-已忽略
- **通知类型**: 1-好友申请, 2-好友通过, 3-群组邀请, 4-入群申请, 5-入群通过, 6-入群拒绝, 7-系统通知, 8-处罚通知
- **处罚类型**: 0-无处罚, 1-警告, 2-禁言, 3-封号, 4-永久封号

---
**版本**: v1.0.0
