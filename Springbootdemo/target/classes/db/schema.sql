-- 数据库表结构示例

CREATE TABLE IF NOT EXISTS conversation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  title VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS message_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  conversation_id BIGINT,
  role VARCHAR(16),
  content TEXT,
  created_at DATETIME
);

CREATE TABLE IF NOT EXISTS article (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  author VARCHAR(64),
  category VARCHAR(64),
  position INT,
  create_time DATETIME,
  update_time DATETIME
);
