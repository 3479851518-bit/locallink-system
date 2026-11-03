DROP TABLE IF EXISTS user_report;
DROP TABLE IF EXISTS sys_notification;
DROP TABLE IF EXISTS file_transfer;
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS group_member;
DROP TABLE IF EXISTS group_info;
DROP TABLE IF EXISTS friend_relation;
DROP TABLE IF EXISTS user_info;

CREATE TABLE user_info (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50),
  nickname VARCHAR(50),
  account VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  avatar VARCHAR(255),
  ip VARCHAR(50),
  port INT,
  mac VARCHAR(50),
  status INT DEFAULT 0,
  role INT DEFAULT 0,
  is_banned INT DEFAULT 0,
  ban_reason VARCHAR(255),
  ban_time TIMESTAMP,
  is_muted INT DEFAULT 0,
  mute_reason VARCHAR(255),
  mute_time TIMESTAMP,
  mute_expire_time TIMESTAMP,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0
);

CREATE TABLE friend_relation (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  remark VARCHAR(50),
  status INT DEFAULT 0,
  is_blocked INT DEFAULT 0,
  apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0
);

CREATE TABLE group_info (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  group_id VARCHAR(50) NOT NULL,
  group_name VARCHAR(100) NOT NULL,
  group_avatar VARCHAR(255),
  creator_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0
);

CREATE TABLE group_member (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  group_id VARCHAR(50) NOT NULL,
  user_id BIGINT NOT NULL,
  role INT DEFAULT 0,
  apply_status INT DEFAULT 0,
  apply_time TIMESTAMP,
  audit_time TIMESTAMP,
  join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0
);

CREATE TABLE chat_message (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT,
  group_id VARCHAR(50),
  chat_type INT DEFAULT 1,
  message_type INT DEFAULT 1,
  content CLOB,
  file_path VARCHAR(255),
  file_status INT DEFAULT 0,
  send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_read INT DEFAULT 0,
  is_recalled INT DEFAULT 0,
  deleted INT DEFAULT 0
);

CREATE TABLE file_transfer (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  task_id VARCHAR(50),
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_size BIGINT,
  file_path VARCHAR(255),
  transferred_bytes BIGINT DEFAULT 0,
  transfer_type INT DEFAULT 1,
  status INT DEFAULT 0,
  start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  complete_time TIMESTAMP,
  deleted INT DEFAULT 0
);

CREATE TABLE sys_notification (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  type INT NOT NULL,
  title VARCHAR(100) NOT NULL,
  content CLOB,
  related_id BIGINT,
  is_read INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  read_time TIMESTAMP
);

CREATE TABLE user_report (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  reporter_id BIGINT NOT NULL,
  reported_user_id BIGINT,
  message_id BIGINT,
  report_type INT DEFAULT 1,
  reason_code INT,
  reason CLOB,
  include_messages INT DEFAULT 0,
  penalty_type INT DEFAULT 0,
  status INT DEFAULT 0,
  handler_id BIGINT,
  handle_result CLOB,
  handle_time TIMESTAMP,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
