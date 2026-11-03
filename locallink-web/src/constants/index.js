/**
 * 用户角色枚举
 */
export const USER_ROLE = {
  NORMAL: 0,
  ADMIN: 1
}

/**
 * 用户在线状态枚举
 */
export const ONLINE_STATUS = {
  OFFLINE: 0,
  ONLINE: 1,
  BUSY: 2
}

/**
 * 在线状态文本映射
 */
export const ONLINE_STATUS_TEXT = {
  [ONLINE_STATUS.OFFLINE]: '离线',
  [ONLINE_STATUS.ONLINE]: '在线',
  [ONLINE_STATUS.BUSY]: '忙碌'
}

/**
 * 聊天类型枚举
 */
export const CHAT_TYPE = {
  PRIVATE: 1,
  GROUP: 2
}

/**
 * 消息类型枚举
 */
export const MESSAGE_TYPE = {
  TEXT: 1,
  IMAGE: 2,
  FILE: 3,
  SYSTEM: 4
}

/**
 * 好友状态枚举
 */
export const FRIEND_STATUS = {
  PENDING: 0,
  ACCEPTED: 1,
  REJECTED: 2
}

/**
 * 举报原因枚举
 */
export const REPORT_REASON = {
  SPAM: 1,        // 垃圾广告
  HARASSMENT: 2,  // 骚扰他人
  FRAUD: 3,       // 欺诈行为
  INAPPROPRIATE: 4, // 不当内容
  IMPERSONATION: 5, // 冒充他人
  PRIVACY: 6,     // 侵犯隐私
  OTHER: 7        // 其他原因
}

/**
 * 举报原因文本映射
 */
export const REPORT_REASON_TEXT = {
  [REPORT_REASON.SPAM]: '发布垃圾广告',
  [REPORT_REASON.HARASSMENT]: '骚扰他人',
  [REPORT_REASON.FRAUD]: '欺诈行为',
  [REPORT_REASON.INAPPROPRIATE]: '不当内容',
  [REPORT_REASON.IMPERSONATION]: '冒充他人',
  [REPORT_REASON.PRIVACY]: '侵犯隐私',
  [REPORT_REASON.OTHER]: '其他原因'
}

/**
 * 举报类型枚举
 */
export const REPORT_TYPE = {
  USER: 1,
  MESSAGE: 2
}

/**
 * 群角色枚举
 */
export const GROUP_ROLE = {
  MEMBER: 0,
  ADMIN: 1,
  OWNER: 2
}

/**
 * 群角色文本映射
 */
export const GROUP_ROLE_TEXT = {
  [GROUP_ROLE.MEMBER]: '成员',
  [GROUP_ROLE.ADMIN]: '管理员',
  [GROUP_ROLE.OWNER]: '群主'
}

/**
 * 性别枚举
 */
export const GENDER = {
  UNKNOWN: 0,
  MALE: 1,
  FEMALE: 2
}

/**
 * 封号/禁言状态
 */
export const BAN_STATUS = {
  NORMAL: 0,
  BANNED: 1
}

export const MUTE_STATUS = {
  NORMAL: 0,
  MUTED: 1
}

/**
 * 举报处理状态
 */
export const REPORT_STATUS = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2
}
